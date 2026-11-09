package br.com.fiap.acessibilidade.dto;

import br.com.fiap.acessibilidade.model.enums.SituacaoValidacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ValidacaoRelatoUpdateRequest {
    @NotNull(message = "A situação é obrigatória (AINDA_EXISTE ou RESOLVIDO)")
    private SituacaoValidacao situacao;

    @Size(max = 300, message = "O comentário deve ter no máximo 300 caracteres")
    private String comentario;
}
