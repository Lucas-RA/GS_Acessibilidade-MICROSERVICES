package br.com.fiap.acessibilidade.dto;

import java.time.LocalDateTime;

import br.com.fiap.acessibilidade.model.enums.CategoriaRelato;
import br.com.fiap.acessibilidade.model.enums.Prioridade;
import br.com.fiap.acessibilidade.model.enums.StatusRelato;
import lombok.Data;

@Data
public class RelatoAcessibilidadeResponse {
    private Long id;
    private String descricao;
    private CategoriaRelato categoria;
    private StatusRelato status;
    private Prioridade prioridade;
    private String fotoUrl;
    private String descricaoFoto;
    private LocalDateTime criadoEm;
    private Long usuarioId;
    private String usuarioNome;
    private Long localId;
    private String localNome;
}
