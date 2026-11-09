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

import br.com.fiap.acessibilidade.dto.ValidacaoRelatoCreateRequest;
import br.com.fiap.acessibilidade.dto.ValidacaoRelatoMapper;
import br.com.fiap.acessibilidade.dto.ValidacaoRelatoResponse;
import br.com.fiap.acessibilidade.dto.ValidacaoRelatoUpdateRequest;
import br.com.fiap.acessibilidade.model.ValidacaoRelato;
import br.com.fiap.acessibilidade.service.ValidacaoRelatoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/${api.version}/validacoes-relato")
public class ValidacaoRelatoController {

    @Autowired
    private ValidacaoRelatoService service;

    @Autowired
    private ValidacaoRelatoMapper validacaoMapper;

    @PostMapping
    public ResponseEntity<ValidacaoRelatoResponse> create(
            @Valid @RequestBody ValidacaoRelatoCreateRequest dtoRequest) {
        Optional<ValidacaoRelato> criada = service.create(validacaoMapper.toModel(dtoRequest),
                dtoRequest.getRelatoId(), dtoRequest.getUsuarioId());
        if (criada.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(validacaoMapper.toDto(criada.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ValidacaoRelatoResponse> findById(@PathVariable Long id) {
        return service
                .findById(id)
                .map(validacao -> validacaoMapper.toDto(validacao))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ValidacaoRelatoResponse>> findAll() {
        return ResponseEntity.ok(service.findAll()
                .stream()
                .map(validacao -> validacaoMapper.toDto(validacao))
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ValidacaoRelatoResponse> update(@PathVariable Long id,
            @Valid @RequestBody ValidacaoRelatoUpdateRequest dtoRequest) {
        Optional<ValidacaoRelato> atualizada = service.update(id, validacaoMapper.toModel(id, dtoRequest));
        if (atualizada.isPresent()) {
            return ResponseEntity.ok(validacaoMapper.toDto(atualizada.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (service.findById(id).isPresent()) {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
