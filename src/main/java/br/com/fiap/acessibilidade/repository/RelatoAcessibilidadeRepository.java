package br.com.fiap.acessibilidade.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.fiap.acessibilidade.model.RelatoAcessibilidade;

@Repository
public interface RelatoAcessibilidadeRepository extends JpaRepository<RelatoAcessibilidade, Long> {

}
