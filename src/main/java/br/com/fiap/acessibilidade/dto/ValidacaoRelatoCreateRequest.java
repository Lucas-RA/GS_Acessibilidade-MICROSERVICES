package br.com.fiap.acessibilidade.dto;

import br.com.fiap.acessibilidade.model.SituacaoValidacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ValidacaoRelatoCreateRequest {
    @NotNull(message = "A situação é obrigatória (AINDA_EXISTE ou RESOLVIDO)")
    private SituacaoValidacao situacao;

    @Size(max = 300, message = "O comentário deve ter no máximo 300 caracteres")
    private String comentario;

    @NotNull(message = "O id do relato é obrigatório")
    private Long relatoId;

    @NotNull(message = "O id do usuário é obrigatório")
    private Long usuarioId;
}
