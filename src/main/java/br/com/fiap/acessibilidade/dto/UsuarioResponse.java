package br.com.fiap.acessibilidade.dto;

import java.time.LocalDateTime;

import br.com.fiap.acessibilidade.model.PerfilMobilidade;
import br.com.fiap.acessibilidade.model.TipoPerfil;
import lombok.Data;

@Data
public class UsuarioResponse {
    private Long id;
    private String nome;
    private String email;
    private TipoPerfil tipoPerfil;
    private PerfilMobilidade perfilMobilidade;
    private LocalDateTime criadoEm;
}
