package br.com.fiap.acessibilidade.model;

import java.time.LocalDateTime;

import br.com.fiap.acessibilidade.model.enums.CategoriaRelato;
import br.com.fiap.acessibilidade.model.enums.Prioridade;
import br.com.fiap.acessibilidade.model.enums.StatusRelato;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "relatos_acessibilidade")
public class RelatoAcessibilidade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaRelato categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusRelato status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridade prioridade;

    @Column(length = 300)
    private String fotoUrl;

    // texto alternativo da foto, lido por leitores de tela
    @Column(length = 300)
    private String descricaoFoto;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "local_id", nullable = false)
    private LocalPublico local;
}
