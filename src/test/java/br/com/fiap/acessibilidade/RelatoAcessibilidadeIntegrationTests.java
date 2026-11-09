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
import br.com.fiap.acessibilidade.model.TipoLocal;
import br.com.fiap.acessibilidade.model.TipoPerfil;
import br.com.fiap.acessibilidade.model.Usuario;
import br.com.fiap.acessibilidade.repository.LocalPublicoRepository;
import br.com.fiap.acessibilidade.repository.RelatoAcessibilidadeRepository;
import br.com.fiap.acessibilidade.repository.UsuarioRepository;
import br.com.fiap.acessibilidade.repository.ValidacaoRelatoRepository;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class RelatoAcessibilidadeIntegrationTests {

    private static final String URL = "/api/v1/relatos-acessibilidade";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RelatoAcessibilidadeRepository relatoRepository;

    @Autowired
    private ValidacaoRelatoRepository validacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LocalPublicoRepository localPublicoRepository;

    private Usuario usuario;
    private LocalPublico local;

    // O H2 é compartilhado entre as classes de teste: apaga primeiro quem tem FK.
    @BeforeEach
    void prepararBanco() {
        validacaoRepository.deleteAll();
        relatoRepository.deleteAll();
        usuarioRepository.deleteAll();
        localPublicoRepository.deleteAll();

        usuario = new Usuario();
        usuario.setNome("Helena Costa");
        usuario.setEmail("helena@email.com");
        usuario.setTipoPerfil(TipoPerfil.CIDADAO);
        usuario.setCriadoEm(LocalDateTime.now());
        usuario = usuarioRepository.save(usuario);

        local = new LocalPublico();
        local.setNome("Terminal Bandeira");
        local.setTipo(TipoLocal.PARADA_ONIBUS);
        local.setEndereco("Praça da Bandeira, s/n - Centro, São Paulo");
        local = localPublicoRepository.save(local);
    }

    @Test
    void deveExecutarCrudDeRelato() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(relatoJson("Calçada com buracos na saída do terminal", "CALCADA",
                                usuario.getId(), local.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"))
                .andExpect(jsonPath("$.usuarioId").value(usuario.getId()))
                .andExpect(jsonPath("$.usuarioNome").value("Helena Costa"))
                .andExpect(jsonPath("$.localId").value(local.getId()))
                .andExpect(jsonPath("$.localNome").value("Terminal Bandeira"))
                .andExpect(jsonPath("$.criadoEm").exists());

        RelatoAcessibilidade criado = relatoRepository.findAll().get(0);

        mockMvc.perform(get(URL + "/" + criado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("CALCADA"))
                .andExpect(jsonPath("$.usuarioNome").value("Helena Costa"));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // categoria nova recalcula a prioridade; usuário e local não mudam
        mockMvc.perform(put(URL + "/" + criado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "Atendente sem treinamento em Libras",
                                  "categoria": "ATENDIMENTO",
                                  "status": "EM_ANALISE",
                                  "fotoUrl": "https://exemplo.com/fotos/balcao.jpg",
                                  "descricaoFoto": "Balcão de atendimento sem intérprete"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(criado.getId()))
                .andExpect(jsonPath("$.categoria").value("ATENDIMENTO"))
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.prioridade").value("BAIXA"))
                .andExpect(jsonPath("$.descricaoFoto").value("Balcão de atendimento sem intérprete"))
                .andExpect(jsonPath("$.usuarioId").value(usuario.getId()))
                .andExpect(jsonPath("$.localId").value(local.getId()));

        RelatoAcessibilidade atualizado = relatoRepository.findById(criado.getId()).get();
        assertEquals(criado.getCriadoEm(), atualizado.getCriadoEm());

        mockMvc.perform(delete(URL + "/" + criado.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(URL + "/" + criado.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveCalcularPrioridadePelaCategoria() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(relatoJson("Piso tátil interrompido na plataforma", "PISO_TATIL",
                                usuario.getId(), local.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prioridade").value("MEDIA"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(relatoJson("Falta banco para descanso na fila", "OUTRO",
                                usuario.getId(), local.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prioridade").value("BAIXA"));
    }

    @Test
    void deveIgnorarStatusEPrioridadeEnviadosPeloCliente() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\": \"Rampa muito inclinada na entrada\", \"categoria\": \"RAMPA\", "
                                + "\"status\": \"RESOLVIDO\", \"prioridade\": \"BAIXA\", "
                                + "\"usuarioId\": " + usuario.getId() + ", \"localId\": " + local.getId() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"));
    }

    @Test
    void deveRetornar404QuandoUsuarioOuLocalNaoExiste() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(relatoJson("Sinalização apagada no ponto", "SINALIZACAO",
                                99999L, local.getId())))
                .andExpect(status().isNotFound());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(relatoJson("Sinalização apagada no ponto", "SINALIZACAO",
                                usuario.getId(), 99999L)))
                .andExpect(status().isNotFound());

        assertEquals(0, relatoRepository.count());
    }

    @Test
    void deveRetornar404ParaRelatoInexistente() throws Exception {
        mockMvc.perform(get(URL + "/99999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(put(URL + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "Descrição qualquer do relato",
                                  "categoria": "OUTRO",
                                  "status": "ABERTO"
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
                .andExpect(jsonPath("$.fields.descricao").value("A descrição do relato é obrigatória"))
                .andExpect(jsonPath("$.fields.categoria").exists())
                .andExpect(jsonPath("$.fields.usuarioId").value("O id do usuário é obrigatório"))
                .andExpect(jsonPath("$.fields.localId").value("O id do local é obrigatório"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(relatoJson("Curta", "RAMPA", usuario.getId(), local.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.descricao").value("A descrição deve ter entre 10 e 500 caracteres"));
    }

    @Test
    void deveExigirStatusNoPut() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(relatoJson("Banheiro adaptado trancado", "BANHEIRO",
                                usuario.getId(), local.getId())))
                .andExpect(status().isCreated());

        RelatoAcessibilidade criado = relatoRepository.findAll().get(0);

        mockMvc.perform(put(URL + "/" + criado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "descricao": "Banheiro adaptado trancado",
                                  "categoria": "BANHEIRO"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.status").value("O status é obrigatório (ABERTO, EM_ANALISE ou RESOLVIDO)"));
    }

    @Test
    void deveRetornar409AoExcluirRelatoComValidacao() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(relatoJson("Elevador da plataforma sem funcionar", "ELEVADOR",
                                usuario.getId(), local.getId())))
                .andExpect(status().isCreated());
        RelatoAcessibilidade relato = relatoRepository.findAll().get(0);

        mockMvc.perform(post("/api/v1/validacoes-relato")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"situacao\": \"AINDA_EXISTE\", \"relatoId\": " + relato.getId()
                                + ", \"usuarioId\": " + usuario.getId() + "}"))
                .andExpect(status().isCreated());

        // as validações precisam ser excluídas antes do relato
        mockMvc.perform(delete(URL + "/" + relato.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        mockMvc.perform(get(URL + "/" + relato.getId()))
                .andExpect(status().isOk());
    }

    private String relatoJson(String descricao, String categoria, Long usuarioId, Long localId) {
        return "{\"descricao\": \"" + descricao + "\", \"categoria\": \"" + categoria + "\", "
                + "\"usuarioId\": " + usuarioId + ", \"localId\": " + localId + "}";
    }
}
