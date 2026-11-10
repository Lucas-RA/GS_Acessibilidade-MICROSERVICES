package br.com.fiap.acessibilidade.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.fiap.acessibilidade.model.Usuario;
import br.com.fiap.acessibilidade.repository.UsuarioRepository;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository repository;

    public Usuario create(Usuario usuario) {
        // e-mail sempre em minúsculas para a constraint unique não aceitar "Ana@x.com" e "ana@x.com"
        usuario.setEmail(usuario.getEmail().trim().toLowerCase());
        usuario.setCriadoEm(LocalDateTime.now());
        usuario.setAtivo(true);
        return repository.save(usuario);
    }

    public Optional<Usuario> findById(Long id) {
        return repository.findById(id);
    }

    public List<Usuario> findAll() {
        return repository.findAll();
    }

    public List<Usuario> findAtivos() {
        List<Usuario> ativos = new ArrayList<>();
        for (Usuario usuario : repository.findAll()) {
            if (usuario.getAtivo()) {
                ativos.add(usuario);
            }
        }
        return ativos;
    }

    public List<Usuario> findInativos() {
        List<Usuario> inativos = new ArrayList<>();
        for (Usuario usuario : repository.findAll()) {
            if (!usuario.getAtivo()) {
                inativos.add(usuario);
            }
        }
        return inativos;
    }

    // Copia só os campos editáveis para o registro do banco, assim criadoEm não se perde no PUT.
    // Usuário desativado foi anonimizado e não volta a ser editado.
    public Optional<Usuario> update(Long id, Usuario dados) {
        Optional<Usuario> existente = repository.findById(id);
        if (existente.isEmpty() || !existente.get().getAtivo()) {
            return Optional.empty();
        }

        Usuario usuario = existente.get();
        usuario.setNome(dados.getNome());
        usuario.setEmail(dados.getEmail().trim().toLowerCase());
        usuario.setTipoPerfil(dados.getTipoPerfil());
        usuario.setPerfilMobilidade(dados.getPerfilMobilidade());
        return Optional.of(repository.save(usuario));
    }

    // A exclusão não apaga o registro: os relatos do usuário continuam úteis para outras pessoas.
    // Os dados pessoais são trocados (LGPD) e o e-mail fica livre para um novo cadastro.
    public void desativar(Long id) {
        Optional<Usuario> existente = repository.findById(id);
        if (existente.isPresent()) {
            Usuario usuario = existente.get();
            usuario.setNome("Usuário removido");
            // sem "@": o @Email dos DTOs recusa esse valor, então ninguém consegue cadastrá-lo antes
            usuario.setEmail("removido-" + usuario.getId());
            usuario.setPerfilMobilidade(null);
            usuario.setAtivo(false);
            repository.save(usuario);
        }
    }
}
