package br.com.fiap.acessibilidade.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.fiap.acessibilidade.model.LocalPublico;
import br.com.fiap.acessibilidade.repository.LocalPublicoRepository;

@Service
public class LocalPublicoService {
    @Autowired
    private LocalPublicoRepository repository;

    public LocalPublico create(LocalPublico local) {
        // todo local nasce ativo; a desativação é feita depois pelo PUT
        local.setAtivo(true);
        return repository.save(local);
    }

    public Optional<LocalPublico> findById(Long id) {
        return repository.findById(id);
    }

    public List<LocalPublico> findAll() {
        return repository.findAll();
    }

    public Optional<LocalPublico> update(Long id, LocalPublico dados) {
        Optional<LocalPublico> existente = repository.findById(id);
        if (existente.isEmpty()) {
            return Optional.empty();
        }

        LocalPublico local = existente.get();
        local.setNome(dados.getNome());
        local.setTipo(dados.getTipo());
        local.setEndereco(dados.getEndereco());
        local.setLatitude(dados.getLatitude());
        local.setLongitude(dados.getLongitude());
        local.setAtivo(dados.getAtivo());
        return Optional.of(repository.save(local));
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
