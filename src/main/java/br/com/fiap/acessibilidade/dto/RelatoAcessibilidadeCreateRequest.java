package br.com.fiap.acessibilidade.dto;

import br.com.fiap.acessibilidade.model.CategoriaRelato;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RelatoAcessibilidadeCreateRequest {
    @NotBlank(message = "A descrição do relato é obrigatória")
    @Size(min = 10, max = 500, message = "A descrição deve ter entre 10 e 500 caracteres")
    private String descricao;

    @NotNull(message = "A categoria é obrigatória (CALCADA, RAMPA, PISO_TATIL, SINALIZACAO, ELEVADOR, BANHEIRO, ATENDIMENTO ou OUTRO)")
    private CategoriaRelato categoria;

    @Size(max = 300, message = "A URL da foto deve ter no máximo 300 caracteres")
    private String fotoUrl;

    @Size(max = 300, message = "A descrição da foto deve ter no máximo 300 caracteres")
    private String descricaoFoto;

    @NotNull(message = "O id do usuário é obrigatório")
    private Long usuarioId;

    @NotNull(message = "O id do local é obrigatório")
    private Long localId;
}
