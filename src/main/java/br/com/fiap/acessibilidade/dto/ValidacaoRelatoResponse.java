package br.com.fiap.acessibilidade.dto;

import java.time.LocalDateTime;

import br.com.fiap.acessibilidade.model.SituacaoValidacao;
import lombok.Data;

@Data
public class ValidacaoRelatoResponse {
    private Long id;
    private SituacaoValidacao situacao;
    private String comentario;
    private LocalDateTime criadoEm;
    private Long relatoId;
    private Long usuarioId;
    private String usuarioNome;
}
