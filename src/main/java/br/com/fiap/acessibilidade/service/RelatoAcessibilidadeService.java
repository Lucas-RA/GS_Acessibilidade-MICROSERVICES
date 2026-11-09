package br.com.fiap.acessibilidade.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.fiap.acessibilidade.model.CategoriaRelato;
import br.com.fiap.acessibilidade.model.LocalPublico;
import br.com.fiap.acessibilidade.model.Prioridade;
import br.com.fiap.acessibilidade.model.RelatoAcessibilidade;
import br.com.fiap.acessibilidade.model.StatusRelato;
import br.com.fiap.acessibilidade.model.Usuario;
import br.com.fiap.acessibilidade.repository.RelatoAcessibilidadeRepository;

@Service
public class RelatoAcessibilidadeService {
    @Autowired
    private RelatoAcessibilidadeRepository repository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private LocalPublicoService localPublicoService;

    // Devolve vazio quando o usuário ou o local não existem; a Controller responde 404.
    public Optional<RelatoAcessibilidade> create(RelatoAcessibilidade relato, Long usuarioId, Long localId) {
        Optional<Usuario> usuario = usuarioService.findById(usuarioId);
        Optional<LocalPublico> local = localPublicoService.findById(localId);
        if (usuario.isEmpty() || local.isEmpty()) {
            return Optional.empty();
        }

        relato.setUsuario(usuario.get());
        relato.setLocal(local.get());
        relato.setStatus(StatusRelato.ABERTO);
        relato.setPrioridade(calcularPrioridade(relato.getCategoria()));
        relato.setCriadoEm(LocalDateTime.now());
        return Optional.of(repository.save(relato));
    }

    public Optional<RelatoAcessibilidade> findById(Long id) {
        return repository.findById(id);
    }

    public List<RelatoAcessibilidade> findAll() {
        return repository.findAll();
    }

    // Usuário, local e criadoEm ficam como estavam; só os campos do relato mudam.
    public Optional<RelatoAcessibilidade> update(Long id, RelatoAcessibilidade dados) {
        Optional<RelatoAcessibilidade> existente = repository.findById(id);
        if (existente.isEmpty()) {
            return Optional.empty();
        }

        RelatoAcessibilidade relato = existente.get();
        relato.setDescricao(dados.getDescricao());
        relato.setCategoria(dados.getCategoria());
        relato.setStatus(dados.getStatus());
        relato.setFotoUrl(dados.getFotoUrl());
        relato.setDescricaoFoto(dados.getDescricaoFoto());
        relato.setPrioridade(calcularPrioridade(dados.getCategoria()));
        return Optional.of(repository.save(relato));
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    // Barreiras que impedem a circulação têm prioridade alta.
    private Prioridade calcularPrioridade(CategoriaRelato categoria) {
        switch (categoria) {
            case CALCADA, RAMPA, ELEVADOR:
                return Prioridade.ALTA;
            case PISO_TATIL, SINALIZACAO, BANHEIRO:
                return Prioridade.MEDIA;
            default:
                return Prioridade.BAIXA;
        }
    }
}
