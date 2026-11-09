package br.com.fiap.acessibilidade.dto;

import br.com.fiap.acessibilidade.model.enums.PerfilMobilidade;
import br.com.fiap.acessibilidade.model.enums.TipoPerfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioUpdateRequest {
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
    private String email;

    @NotNull(message = "O tipo de perfil é obrigatório (CIDADAO ou GESTOR)")
    private TipoPerfil tipoPerfil;

    private PerfilMobilidade perfilMobilidade;
}
