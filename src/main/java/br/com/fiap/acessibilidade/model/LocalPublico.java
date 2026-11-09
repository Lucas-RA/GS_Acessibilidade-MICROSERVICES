package br.com.fiap.acessibilidade.model;

import br.com.fiap.acessibilidade.model.enums.TipoLocal;
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
@Table(name = "locais_publicos")
public class LocalPublico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoLocal tipo;

    @Column(nullable = false, length = 200)
    private String endereco;

    private Double latitude;

    private Double longitude;

    @Column(nullable = false)
    private Boolean ativo = true;
}
