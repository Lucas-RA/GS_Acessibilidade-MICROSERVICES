package br.com.fiap.acessibilidade.model;

import java.time.LocalDateTime;

import br.com.fiap.acessibilidade.model.enums.PerfilMobilidade;
import br.com.fiap.acessibilidade.model.enums.TipoPerfil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPerfil tipoPerfil;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PerfilMobilidade perfilMobilidade;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    // Usuário que pediu exclusão fica inativo e anonimizado; os relatos dele continuam no mapa.
    @Column(nullable = false)
    private Boolean ativo = true;
}
