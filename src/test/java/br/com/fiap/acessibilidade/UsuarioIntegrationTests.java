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

import br.com.fiap.acessibilidade.model.LocalPublico;
import br.com.fiap.acessibilidade.model.Usuario;
import br.com.fiap.acessibilidade.repository.LocalPublicoRepository;
import br.com.fiap.acessibilidade.repository.RelatoAcessibilidadeRepository;
import br.com.fiap.acessibilidade.repository.UsuarioRepository;
import br.com.fiap.acessibilidade.repository.ValidacaoRelatoRepository;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UsuarioIntegrationTests {

    private static final String URL = "/api/v1/usuarios";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LocalPublicoRepository localPublicoRepository;

    @Autowired
    private RelatoAcessibilidadeRepository relatoRepository;

    @Autowired
    private ValidacaoRelatoRepository validacaoRepository;

    // O H2 é compartilhado entre as classes de teste: apaga primeiro quem tem FK.
    @BeforeEach
    void cleanDatabase() {
        validacaoRepository.deleteAll();
        relatoRepository.deleteAll();
        usuarioRepository.deleteAll();
        localPublicoRepository.deleteAll();
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
                .andExpect(jsonPath("$.criadoEm").exists())
                .andExpect(jsonPath("$.ativo").value(true));

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

        // o DELETE desativa e anonimiza: o registro continua, sem os dados pessoais
        mockMvc.perform(delete(URL + "/" + criado.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(URL + "/" + criado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false))
                .andExpect(jsonPath("$.nome").value("Usuário removido"))
                .andExpect(jsonPath("$.email").value("removido-" + criado.getId() + "@anonimo.local"))
                .andExpect(jsonPath("$.perfilMobilidade").isEmpty());
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

    @Test
    void deveDesativarUsuarioComRelatoMantendoORelato() throws Exception {
        criarUsuario("Fabio Nunes", "fabio@email.com");
        Usuario fabio = usuarioRepository.findAll().get(0);

        mockMvc.perform(post("/api/v1/locais-publicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Parada Rua Vergueiro",
                                  "tipo": "PARADA_ONIBUS",
                                  "endereco": "Rua Vergueiro, 1000"
                                }
                                """))
                .andExpect(status().isCreated());
        LocalPublico local = localPublicoRepository.findAll().get(0);

        mockMvc.perform(post("/api/v1/relatos-acessibilidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\": \"Rampa de acesso quebrada\", \"categoria\": \"RAMPA\", "
                                + "\"usuarioId\": " + fabio.getId() + ", \"localId\": " + local.getId() + "}"))
                .andExpect(status().isCreated());

        Long relatoId = relatoRepository.findAll().get(0).getId();

        // o relato continua no mapa, agora sem o nome de quem fez
        mockMvc.perform(delete(URL + "/" + fabio.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/relatos-acessibilidade/" + relatoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(fabio.getId()))
                .andExpect(jsonPath("$.usuarioNome").value("Usuário removido"));

        // usuário desativado não registra relatos novos
        mockMvc.perform(post("/api/v1/relatos-acessibilidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\": \"Calçada esburacada na esquina\", \"categoria\": \"CALCADA\", "
                                + "\"usuarioId\": " + fabio.getId() + ", \"localId\": " + local.getId() + "}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveDesativarUsuarioComValidacao() throws Exception {
        criarUsuario("Gustavo Alves", "gustavo@email.com");
        criarUsuario("Heloisa Matos", "heloisa@email.com");
        Usuario autor = buscarPorEmail("gustavo@email.com");
        Usuario validador = buscarPorEmail("heloisa@email.com");

        mockMvc.perform(post("/api/v1/locais-publicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Parada Av. Brigadeiro",
                                  "tipo": "PARADA_ONIBUS",
                                  "endereco": "Av. Brigadeiro Luís Antônio, 2000"
                                }
                                """))
                .andExpect(status().isCreated());
        LocalPublico local = localPublicoRepository.findAll().get(0);

        mockMvc.perform(post("/api/v1/relatos-acessibilidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\": \"Piso tátil solto perto do meio-fio\", \"categoria\": \"PISO_TATIL\", "
                                + "\"usuarioId\": " + autor.getId() + ", \"localId\": " + local.getId() + "}"))
                .andExpect(status().isCreated());
        Long relatoId = relatoRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/v1/validacoes-relato")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"situacao\": \"AINDA_EXISTE\", \"relatoId\": " + relatoId
                                + ", \"usuarioId\": " + validador.getId() + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete(URL + "/" + validador.getId()))
                .andExpect(status().isNoContent());

        // a validação continua registrada
        mockMvc.perform(get("/api/v1/validacoes-relato"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].usuarioNome").value("Usuário removido"));

        // usuária desativada não valida outros relatos
        mockMvc.perform(post("/api/v1/validacoes-relato")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"situacao\": \"RESOLVIDO\", \"relatoId\": " + relatoId
                                + ", \"usuarioId\": " + validador.getId() + "}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveLiberarEmailEImpedirEdicaoAposDesativar() throws Exception {
        criarUsuario("Igor Mendes", "igor@email.com");
        Usuario igor = usuarioRepository.findAll().get(0);

        mockMvc.perform(delete(URL + "/" + igor.getId()))
                .andExpect(status().isNoContent());

        // o e-mail foi anonimizado, então pode ser usado num cadastro novo
        criarUsuario("Igor Mendes", "igor@email.com");

        // a conta desativada não volta a ser editada
        mockMvc.perform(put(URL + "/" + igor.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Igor Mendes\", \"email\": \"igor.mendes@email.com\", \"tipoPerfil\": \"CIDADAO\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarUsuariosAtivosEInativos() throws Exception {
        criarUsuario("Julia Ramos", "julia@email.com");
        criarUsuario("Kleber Dias", "kleber@email.com");
        Usuario kleber = buscarPorEmail("kleber@email.com");

        mockMvc.perform(delete(URL + "/" + kleber.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get(URL + "/ativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Julia Ramos"));

        mockMvc.perform(get(URL + "/inativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(kleber.getId()))
                .andExpect(jsonPath("$[0].ativo").value(false));
    }

    private Usuario buscarPorEmail(String email) {
        return usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getEmail().equals(email))
                .findFirst()
                .get();
    }

    private void criarUsuario(String nome, String email) throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"" + nome + "\", \"email\": \"" + email + "\", \"tipoPerfil\": \"CIDADAO\"}"))
                .andExpect(status().isCreated());
    }
}
