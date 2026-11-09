package br.com.fiap.acessibilidade;

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
import br.com.fiap.acessibilidade.repository.LocalPublicoRepository;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class LocalPublicoIntegrationTests {

    private static final String URL = "/api/v1/locais-publicos";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LocalPublicoRepository localPublicoRepository;

    @BeforeEach
    void cleanDatabase() {
        localPublicoRepository.deleteAll();
    }

    @Test
    void deveExecutarCrudDeLocalPublico() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Poupatempo Sé",
                                  "tipo": "PONTO_ATENDIMENTO",
                                  "endereco": "Praça do Carmo, s/n - Sé, São Paulo",
                                  "latitude": -23.5505,
                                  "longitude": -46.6333
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.tipo").value("PONTO_ATENDIMENTO"))
                .andExpect(jsonPath("$.latitude").value(-23.5505))
                .andExpect(jsonPath("$.ativo").value(true));

        LocalPublico criado = localPublicoRepository.findAll().get(0);

        mockMvc.perform(get(URL + "/" + criado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Poupatempo Sé"));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // PUT desativando o local, que é a alternativa à exclusão
        mockMvc.perform(put(URL + "/" + criado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Poupatempo Sé",
                                  "tipo": "PONTO_ATENDIMENTO",
                                  "endereco": "Rua do Carmo, 88 - Sé, São Paulo",
                                  "latitude": -23.5510,
                                  "longitude": -46.6320,
                                  "ativo": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(criado.getId()))
                .andExpect(jsonPath("$.endereco").value("Rua do Carmo, 88 - Sé, São Paulo"))
                .andExpect(jsonPath("$.ativo").value(false));

        mockMvc.perform(delete(URL + "/" + criado.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(URL + "/" + criado.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveCriarLocalSemCoordenadas() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Parada Av. Paulista, 1000",
                                  "tipo": "PARADA_ONIBUS",
                                  "endereco": "Av. Paulista, 1000 - Bela Vista, São Paulo"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.latitude").isEmpty())
                .andExpect(jsonPath("$.longitude").isEmpty())
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void deveRetornar404ParaLocalInexistente() throws Exception {
        mockMvc.perform(get(URL + "/99999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(put(URL + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Local",
                                  "tipo": "PARADA_ONIBUS",
                                  "endereco": "Rua A, 1",
                                  "ativo": true
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
                .andExpect(jsonPath("$.fields.nome").value("O nome do local é obrigatório"))
                .andExpect(jsonPath("$.fields.tipo")
                        .value("O tipo do local é obrigatório (PONTO_ATENDIMENTO ou PARADA_ONIBUS)"))
                .andExpect(jsonPath("$.fields.endereco").value("O endereço é obrigatório"));
    }

    @Test
    void deveRetornar400ParaCoordenadasForaDoIntervalo() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Local com coordenada errada",
                                  "tipo": "PARADA_ONIBUS",
                                  "endereco": "Rua B, 2",
                                  "latitude": 90.5,
                                  "longitude": -180.1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.latitude").value("A latitude deve estar entre -90 e 90"))
                .andExpect(jsonPath("$.fields.longitude").value("A longitude deve estar entre -180 e 180"));
    }

    @Test
    void deveExigirCampoAtivoNoPut() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Parada Rua Augusta",
                                  "tipo": "PARADA_ONIBUS",
                                  "endereco": "Rua Augusta, 500"
                                }
                                """))
                .andExpect(status().isCreated());

        LocalPublico criado = localPublicoRepository.findAll().get(0);

        mockMvc.perform(put(URL + "/" + criado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Parada Rua Augusta",
                                  "tipo": "PARADA_ONIBUS",
                                  "endereco": "Rua Augusta, 500"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.ativo").value("Informe se o local está ativo (true ou false)"));
    }
}
