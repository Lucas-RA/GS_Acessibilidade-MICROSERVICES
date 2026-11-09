package br.com.fiap.acessibilidade.dto;

import br.com.fiap.acessibilidade.model.CategoriaRelato;
import br.com.fiap.acessibilidade.model.StatusRelato;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RelatoAcessibilidadeUpdateRequest {
    @NotBlank(message = "A descrição do relato é obrigatória")
    @Size(min = 10, max = 500, message = "A descrição deve ter entre 10 e 500 caracteres")
    private String descricao;

    @NotNull(message = "A categoria é obrigatória (CALCADA, RAMPA, PISO_TATIL, SINALIZACAO, ELEVADOR, BANHEIRO, ATENDIMENTO ou OUTRO)")
    private CategoriaRelato categoria;

    @NotNull(message = "O status é obrigatório (ABERTO, EM_ANALISE ou RESOLVIDO)")
    private StatusRelato status;

    @Size(max = 300, message = "A URL da foto deve ter no máximo 300 caracteres")
    private String fotoUrl;

    @Size(max = 300, message = "A descrição da foto deve ter no máximo 300 caracteres")
    private String descricaoFoto;
}
