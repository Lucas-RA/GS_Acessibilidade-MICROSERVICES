package br.com.fiap.acessibilidade.dto;

import br.com.fiap.acessibilidade.model.enums.TipoLocal;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LocalPublicoCreateRequest {
    @NotBlank(message = "O nome do local é obrigatório")
    @Size(max = 120, message = "O nome do local deve ter no máximo 120 caracteres")
    private String nome;

    @NotNull(message = "O tipo do local é obrigatório (PONTO_ATENDIMENTO ou PARADA_ONIBUS)")
    private TipoLocal tipo;

    @NotBlank(message = "O endereço é obrigatório")
    @Size(max = 200, message = "O endereço deve ter no máximo 200 caracteres")
    private String endereco;

    @Min(value = -90, message = "A latitude deve estar entre -90 e 90")
    @Max(value = 90, message = "A latitude deve estar entre -90 e 90")
    private Double latitude;

    @Min(value = -180, message = "A longitude deve estar entre -180 e 180")
    @Max(value = 180, message = "A longitude deve estar entre -180 e 180")
    private Double longitude;
}
