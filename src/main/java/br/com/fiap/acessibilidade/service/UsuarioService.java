package br.com.fiap.acessibilidade.service;

import java.time.LocalDateTime;
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
        return repository.save(usuario);
    }

    public Optional<Usuario> findById(Long id) {
        return repository.findById(id);
    }

    public List<Usuario> findAll() {
        return repository.findAll();
    }

    // Copia só os campos editáveis para o registro do banco, assim criadoEm não se perde no PUT.
    public Optional<Usuario> update(Long id, Usuario dados) {
        Optional<Usuario> existente = repository.findById(id);
        if (existente.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = existente.get();
        usuario.setNome(dados.getNome());
        usuario.setEmail(dados.getEmail().trim().toLowerCase());
        usuario.setTipoPerfil(dados.getTipoPerfil());
        usuario.setPerfilMobilidade(dados.getPerfilMobilidade());
        return Optional.of(repository.save(usuario));
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
