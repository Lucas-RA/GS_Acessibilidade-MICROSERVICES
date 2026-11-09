package br.com.fiap.acessibilidade.model;

import java.time.LocalDateTime;

import br.com.fiap.acessibilidade.model.enums.SituacaoValidacao;
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
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

// Cada usuário valida um mesmo relato uma única vez.
@Data
@Entity
@Table(name = "validacoes_relato", uniqueConstraints = @UniqueConstraint(columnNames = {"relato_id", "usuario_id"}))
public class ValidacaoRelato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoValidacao situacao;

    @Column(length = 300)
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @ManyToOne
    @JoinColumn(name = "relato_id", nullable = false)
    private RelatoAcessibilidade relato;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
