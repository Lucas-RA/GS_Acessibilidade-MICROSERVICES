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

import br.com.fiap.acessibilidade.dto.LocalPublicoCreateRequest;
import br.com.fiap.acessibilidade.dto.LocalPublicoMapper;
import br.com.fiap.acessibilidade.dto.LocalPublicoResponse;
import br.com.fiap.acessibilidade.dto.LocalPublicoUpdateRequest;
import br.com.fiap.acessibilidade.model.LocalPublico;
import br.com.fiap.acessibilidade.service.LocalPublicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Locais públicos", description = "Pontos de atendimento e paradas de ônibus avaliados pela comunidade")
@RestController
@RequestMapping("api/${api.version}/locais-publicos")
public class LocalPublicoController {

    @Autowired
    private LocalPublicoService service;

    @Autowired
    private LocalPublicoMapper localPublicoMapper;

    @Operation(summary = "Cadastra um local público")
    @PostMapping
    public ResponseEntity<LocalPublicoResponse> create(@Valid @RequestBody LocalPublicoCreateRequest dtoRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(localPublicoMapper.toDto(service.create(localPublicoMapper.toModel(dtoRequest))));
    }

    @Operation(summary = "Busca um local público pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<LocalPublicoResponse> findById(@PathVariable Long id) {
        return service
                .findById(id)
                .map(local -> localPublicoMapper.toDto(local))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Lista todos os locais públicos")
    @GetMapping
    public ResponseEntity<List<LocalPublicoResponse>> findAll() {
        return ResponseEntity.ok(service.findAll()
                .stream()
                .map(local -> localPublicoMapper.toDto(local))
                .toList());
    }

    @Operation(summary = "Atualiza um local público, inclusive para desativá-lo")
    @PutMapping("/{id}")
    public ResponseEntity<LocalPublicoResponse> update(@PathVariable Long id,
            @Valid @RequestBody LocalPublicoUpdateRequest dtoRequest) {
        Optional<LocalPublico> atualizado = service.update(id, localPublicoMapper.toModel(id, dtoRequest));
        if (atualizado.isPresent()) {
            return ResponseEntity.ok(localPublicoMapper.toDto(atualizado.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Exclui um local público sem relatos")
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
