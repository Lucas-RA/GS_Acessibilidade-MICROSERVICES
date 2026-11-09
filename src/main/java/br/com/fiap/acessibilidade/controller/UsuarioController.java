package br.com.fiap.acessibilidade.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.acessibilidade.dto.UsuarioCreateRequest;
import br.com.fiap.acessibilidade.dto.UsuarioMapper;
import br.com.fiap.acessibilidade.dto.UsuarioResponse;
import br.com.fiap.acessibilidade.dto.UsuarioUpdateRequest;
import br.com.fiap.acessibilidade.model.Usuario;
import br.com.fiap.acessibilidade.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Usuários", description = "Cadastro de cidadãos e gestores que usam o mapa de acessibilidade")
@RestController
@RequestMapping("api/${api.version}/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Operation(summary = "Cadastra um usuário")
    @PostMapping
    public ResponseEntity<UsuarioResponse> create(@Valid @RequestBody UsuarioCreateRequest dtoRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioMapper.toDto(service.create(usuarioMapper.toModel(dtoRequest))));
    }

    @Operation(summary = "Busca um usuário pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> findById(@PathVariable Long id) {
        return service
                .findById(id)
                .map(usuario -> usuarioMapper.toDto(usuario))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lista todos os usuários")
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> findAll() {
        return ResponseEntity.ok(service.findAll()
                .stream()
                .map(usuario -> usuarioMapper.toDto(usuario))
                .toList());
    }

    @Operation(summary = "Lista só os usuários ativos")
    @GetMapping("/ativos")
    public ResponseEntity<List<UsuarioResponse>> findAtivos() {
        return ResponseEntity.ok(service.findAtivos()
                .stream()
                .map(usuario -> usuarioMapper.toDto(usuario))
                .toList());
    }

    @Operation(summary = "Lista só os usuários desativados")
    @GetMapping("/inativos")
    public ResponseEntity<List<UsuarioResponse>> findInativos() {
        return ResponseEntity.ok(service.findInativos()
                .stream()
                .map(usuario -> usuarioMapper.toDto(usuario))
                .toList());
    }

    @Operation(summary = "Atualiza os dados de um usuário ativo")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> update(@PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest dtoRequest) {
        Optional<Usuario> atualizado = service.update(id, usuarioMapper.toModel(id, dtoRequest));
        if (atualizado.isPresent()) {
            return ResponseEntity.ok(usuarioMapper.toDto(atualizado.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Desativa o usuário e anonimiza os dados pessoais, mantendo os relatos")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (service.findById(id).isPresent()) {
            service.desativar(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
