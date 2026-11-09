package br.com.fiap.acessibilidade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.acessibilidade.model.Usuario;
import br.com.fiap.acessibilidade.repository.UsuarioRepository;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UsuarioIntegrationTests {

    private static final String URL = "/api/v1/usuarios";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void cleanDatabase() {
        usuarioRepository.deleteAll();
    }

    @Test
    void deveExecutarCrudDeUsuario() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Silva",
                                  "email": "Ana.Silva@Email.com",
                                  "tipoPerfil": "CIDADAO",
                                  "perfilMobilidade": "CADEIRA_DE_RODAS"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("ana.silva@email.com"))
                .andExpect(jsonPath("$.tipoPerfil").value("CIDADAO"))
                .andExpect(jsonPath("$.perfilMobilidade").value("CADEIRA_DE_RODAS"))
                .andExpect(jsonPath("$.criadoEm").exists());

        Usuario criado = usuarioRepository.findAll().get(0);

        mockMvc.perform(get(URL + "/" + criado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Silva"));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(put(URL + "/" + criado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Ana Souza",
                                  "email": "ana.souza@email.com",
                                  "tipoPerfil": "GESTOR"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(criado.getId()))
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.email").value("ana.souza@email.com"))
                .andExpect(jsonPath("$.tipoPerfil").value("GESTOR"))
                .andExpect(jsonPath("$.perfilMobilidade").isEmpty());

        // o PUT não pode apagar a data de cadastro
        Usuario atualizado = usuarioRepository.findById(criado.getId()).get();
        assertEquals(criado.getCriadoEm(), atualizado.getCriadoEm());

        mockMvc.perform(delete(URL + "/" + criado.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(URL + "/" + criado.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404ParaUsuarioInexistente() throws Exception {
        mockMvc.perform(get(URL + "/99999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(put(URL + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Bruno Lima",
                                  "email": "bruno@email.com",
                                  "tipoPerfil": "CIDADAO"
                                }
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete(URL + "/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400ComAsMensagensDosCampos() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados inválidos"))
                .andExpect(jsonPath("$.path").value(URL))
                .andExpect(jsonPath("$.fields.nome").value("O nome é obrigatório"))
                .andExpect(jsonPath("$.fields.email").value("O e-mail é obrigatório"))
                .andExpect(jsonPath("$.fields.tipoPerfil").value("O tipo de perfil é obrigatório (CIDADAO ou GESTOR)"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Al",
                                  "email": "email-sem-arroba",
                                  "tipoPerfil": "CIDADAO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.nome").value("O nome deve ter entre 3 e 100 caracteres"))
                .andExpect(jsonPath("$.fields.email").value("Informe um e-mail válido"));
    }

    @Test
    void deveRetornar409ParaEmailDuplicado() throws Exception {
        criarUsuario("Carla Dias", "carla@email.com");

        // mesmo e-mail com letras maiúsculas: o Service normaliza e a constraint unique barra
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Carla Duplicada",
                                  "email": "CARLA@Email.com",
                                  "tipoPerfil": "CIDADAO"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void deveRetornar409AoTrocarParaEmailDeOutroUsuario() throws Exception {
        criarUsuario("Diego Rocha", "diego@email.com");
        criarUsuario("Elisa Prado", "elisa@email.com");

        Usuario elisa = usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getEmail().equals("elisa@email.com"))
                .findFirst()
                .get();

        mockMvc.perform(put(URL + "/" + elisa.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Elisa Prado",
                                  "email": "diego@email.com",
                                  "tipoPerfil": "CIDADAO"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    private void criarUsuario(String nome, String email) throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"" + nome + "\", \"email\": \"" + email + "\", \"tipoPerfil\": \"CIDADAO\"}"))
                .andExpect(status().isCreated());
    }
}
