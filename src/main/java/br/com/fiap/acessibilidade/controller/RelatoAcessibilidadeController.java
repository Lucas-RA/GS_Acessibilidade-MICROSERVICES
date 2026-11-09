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

import br.com.fiap.acessibilidade.dto.RelatoAcessibilidadeCreateRequest;
import br.com.fiap.acessibilidade.dto.RelatoAcessibilidadeMapper;
import br.com.fiap.acessibilidade.dto.RelatoAcessibilidadeResponse;
import br.com.fiap.acessibilidade.dto.RelatoAcessibilidadeUpdateRequest;
import br.com.fiap.acessibilidade.model.RelatoAcessibilidade;
import br.com.fiap.acessibilidade.service.RelatoAcessibilidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Relatos de acessibilidade", description = "Barreiras de acessibilidade relatadas pelos usuários, com prioridade calculada pela categoria")
@RestController
@RequestMapping("api/${api.version}/relatos-acessibilidade")
public class RelatoAcessibilidadeController {

    @Autowired
    private RelatoAcessibilidadeService service;

    @Autowired
    private RelatoAcessibilidadeMapper relatoMapper;

    @Operation(summary = "Registra um relato com status ABERTO e prioridade calculada")
    @PostMapping
    public ResponseEntity<RelatoAcessibilidadeResponse> create(
            @Valid @RequestBody RelatoAcessibilidadeCreateRequest dtoRequest) {
        Optional<RelatoAcessibilidade> criado = service.create(relatoMapper.toModel(dtoRequest),
                dtoRequest.getUsuarioId(), dtoRequest.getLocalId());
        if (criado.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(relatoMapper.toDto(criado.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Busca um relato pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<RelatoAcessibilidadeResponse> findById(@PathVariable Long id) {
        return service
                .findById(id)
                .map(relato -> relatoMapper.toDto(relato))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lista todos os relatos")
    @GetMapping
    public ResponseEntity<List<RelatoAcessibilidadeResponse>> findAll() {
        return ResponseEntity.ok(service.findAll()
                .stream()
                .map(relato -> relatoMapper.toDto(relato))
                .toList());
    }

    @Operation(summary = "Atualiza um relato e recalcula a prioridade")
    @PutMapping("/{id}")
    public ResponseEntity<RelatoAcessibilidadeResponse> update(@PathVariable Long id,
            @Valid @RequestBody RelatoAcessibilidadeUpdateRequest dtoRequest) {
        Optional<RelatoAcessibilidade> atualizado = service.update(id, relatoMapper.toModel(id, dtoRequest));
        if (atualizado.isPresent()) {
            return ResponseEntity.ok(relatoMapper.toDto(atualizado.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Exclui um relato sem validações")
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
