package br.com.fiap.acessibilidade.dto;

import java.time.LocalDateTime;

import br.com.fiap.acessibilidade.model.enums.PerfilMobilidade;
import br.com.fiap.acessibilidade.model.enums.TipoPerfil;
import lombok.Data;

@Data
public class UsuarioResponse {
    private Long id;
    private String nome;
    private String email;
    private TipoPerfil tipoPerfil;
    private PerfilMobilidade perfilMobilidade;
    private LocalDateTime criadoEm;
    private Boolean ativo;
}
