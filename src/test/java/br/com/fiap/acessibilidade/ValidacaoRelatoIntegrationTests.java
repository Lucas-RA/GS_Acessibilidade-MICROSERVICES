package br.com.fiap.acessibilidade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.acessibilidade.model.LocalPublico;
import br.com.fiap.acessibilidade.model.RelatoAcessibilidade;
import br.com.fiap.acessibilidade.model.Usuario;
import br.com.fiap.acessibilidade.model.ValidacaoRelato;
import br.com.fiap.acessibilidade.model.enums.CategoriaRelato;
import br.com.fiap.acessibilidade.model.enums.Prioridade;
import br.com.fiap.acessibilidade.model.enums.StatusRelato;
import br.com.fiap.acessibilidade.model.enums.TipoLocal;
import br.com.fiap.acessibilidade.model.enums.TipoPerfil;
import br.com.fiap.acessibilidade.repository.LocalPublicoRepository;
import br.com.fiap.acessibilidade.repository.RelatoAcessibilidadeRepository;
import br.com.fiap.acessibilidade.repository.UsuarioRepository;
import br.com.fiap.acessibilidade.repository.ValidacaoRelatoRepository;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ValidacaoRelatoIntegrationTests {

    private static final String URL = "/api/v1/validacoes-relato";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ValidacaoRelatoRepository validacaoRepository;

    @Autowired
    private RelatoAcessibilidadeRepository relatoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LocalPublicoRepository localPublicoRepository;

    private Usuario autor;
    private Usuario validador;
    private RelatoAcessibilidade relato;

    // O H2 é compartilhado entre as classes de teste: apaga primeiro quem tem FK.
    @BeforeEach
    void prepararBanco() {
        validacaoRepository.deleteAll();
        relatoRepository.deleteAll();
        usuarioRepository.deleteAll();
        localPublicoRepository.deleteAll();

        autor = salvarUsuario("Igor Mendes", "igor@email.com");
        validador = salvarUsuario("Julia Ramos", "julia@email.com");

        LocalPublico local = new LocalPublico();
        local.setNome("Parada Av. Rebouças, 1500");
        local.setTipo(TipoLocal.PARADA_ONIBUS);
        local.setEndereco("Av. Rebouças, 1500 - Pinheiros, São Paulo");
        local = localPublicoRepository.save(local);

        relato = new RelatoAcessibilidade();
        relato.setDescricao("Rampa da calçada bloqueada por entulho");
        relato.setCategoria(CategoriaRelato.RAMPA);
        relato.setStatus(StatusRelato.ABERTO);
        relato.setPrioridade(Prioridade.ALTA);
        relato.setCriadoEm(LocalDateTime.now());
        relato.setUsuario(autor);
        relato.setLocal(local);
        relato = relatoRepository.save(relato);
    }

    @Test
    void deveExecutarCrudDeValidacao() throws Exception {
        // o usuarioId da resposta precisa ser o de quem validou, não o do autor do relato
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validacaoJson("AINDA_EXISTE", relato.getId(), validador.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.situacao").value("AINDA_EXISTE"))
                .andExpect(jsonPath("$.relatoId").value(relato.getId()))
                .andExpect(jsonPath("$.usuarioId").value(validador.getId()))
                .andExpect(jsonPath("$.usuarioNome").value("Julia Ramos"))
                .andExpect(jsonPath("$.criadoEm").exists());

        ValidacaoRelato criada = validacaoRepository.findAll().get(0);

        mockMvc.perform(get(URL + "/" + criada.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(validador.getId()));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(put(URL + "/" + criada.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "situacao": "RESOLVIDO",
                                  "comentario": "Entulho retirado, rampa livre"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(criada.getId()))
                .andExpect(jsonPath("$.situacao").value("RESOLVIDO"))
                .andExpect(jsonPath("$.comentario").value("Entulho retirado, rampa livre"))
                .andExpect(jsonPath("$.relatoId").value(relato.getId()))
                .andExpect(jsonPath("$.usuarioId").value(validador.getId()));

        ValidacaoRelato atualizada = validacaoRepository.findById(criada.getId()).get();
        assertEquals(criada.getCriadoEm(), atualizada.getCriadoEm());

        mockMvc.perform(delete(URL + "/" + criada.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(URL + "/" + criada.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409ParaValidacaoDuplicada() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validacaoJson("AINDA_EXISTE", relato.getId(), validador.getId())))
                .andExpect(status().isCreated());

        // mesmo usuário no mesmo relato: a constraint única barra
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validacaoJson("RESOLVIDO", relato.getId(), validador.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").isNotEmpty());

        // outro usuário no mesmo relato pode validar
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validacaoJson("AINDA_EXISTE", relato.getId(), autor.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(autor.getId()));

        assertEquals(2, validacaoRepository.count());
    }

    @Test
    void deveRetornar404QuandoRelatoOuUsuarioNaoExiste() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validacaoJson("AINDA_EXISTE", 99999L, validador.getId())))
                .andExpect(status().isNotFound());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validacaoJson("AINDA_EXISTE", relato.getId(), 99999L)))
                .andExpect(status().isNotFound());

        assertEquals(0, validacaoRepository.count());
    }

    @Test
    void deveRetornar404ParaValidacaoInexistente() throws Exception {
        mockMvc.perform(get(URL + "/99999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(put(URL + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"situacao\": \"RESOLVIDO\"}"))
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
                .andExpect(jsonPath("$.fields.situacao").value("A situação é obrigatória (AINDA_EXISTE ou RESOLVIDO)"))
                .andExpect(jsonPath("$.fields.relatoId").value("O id do relato é obrigatório"))
                .andExpect(jsonPath("$.fields.usuarioId").value("O id do usuário é obrigatório"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"situacao\": \"AINDA_EXISTE\", \"comentario\": \"" + "a".repeat(301)
                                + "\", \"relatoId\": " + relato.getId() + ", \"usuarioId\": " + validador.getId() + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.comentario").value("O comentário deve ter no máximo 300 caracteres"));
    }

    private Usuario salvarUsuario(String nome, String email) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTipoPerfil(TipoPerfil.CIDADAO);
        usuario.setCriadoEm(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    private String validacaoJson(String situacao, Long relatoId, Long usuarioId) {
        return "{\"situacao\": \"" + situacao + "\", \"relatoId\": " + relatoId + ", \"usuarioId\": " + usuarioId + "}";
    }
}
