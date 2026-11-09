package br.com.fiap.acessibilidade.dto;

import br.com.fiap.acessibilidade.model.enums.TipoLocal;
import lombok.Data;

@Data
public class LocalPublicoResponse {
    private Long id;
    private String nome;
    private TipoLocal tipo;
    private String endereco;
    private Double latitude;
    private Double longitude;
    private Boolean ativo;
}
