package br.com.fiap.acessibilidade.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.fiap.acessibilidade.model.RelatoAcessibilidade;
import br.com.fiap.acessibilidade.model.Usuario;
import br.com.fiap.acessibilidade.model.ValidacaoRelato;
import br.com.fiap.acessibilidade.repository.ValidacaoRelatoRepository;

@Service
public class ValidacaoRelatoService {
    @Autowired
    private ValidacaoRelatoRepository repository;

    @Autowired
    private RelatoAcessibilidadeService relatoService;

    @Autowired
    private UsuarioService usuarioService;

    // Devolve vazio quando o relato ou o usuário não existem, ou quando o usuário foi desativado;
    // a Controller responde 404.
    public Optional<ValidacaoRelato> create(ValidacaoRelato validacao, Long relatoId, Long usuarioId) {
        Optional<RelatoAcessibilidade> relato = relatoService.findById(relatoId);
        Optional<Usuario> usuario = usuarioService.findById(usuarioId);
        if (relato.isEmpty() || usuario.isEmpty() || !usuario.get().getAtivo()) {
            return Optional.empty();
        }

        validacao.setRelato(relato.get());
        validacao.setUsuario(usuario.get());
        validacao.setCriadoEm(LocalDateTime.now());
        return Optional.of(repository.save(validacao));
    }

    public Optional<ValidacaoRelato> findById(Long id) {
        return repository.findById(id);
    }

    public List<ValidacaoRelato> findAll() {
        return repository.findAll();
    }

    // Relato, usuário e criadoEm ficam como estavam; só situação e comentário mudam.
    public Optional<ValidacaoRelato> update(Long id, ValidacaoRelato dados) {
        Optional<ValidacaoRelato> existente = repository.findById(id);
        if (existente.isEmpty()) {
            return Optional.empty();
        }

        ValidacaoRelato validacao = existente.get();
        validacao.setSituacao(dados.getSituacao());
        validacao.setComentario(dados.getComentario());
        return Optional.of(repository.save(validacao));
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
