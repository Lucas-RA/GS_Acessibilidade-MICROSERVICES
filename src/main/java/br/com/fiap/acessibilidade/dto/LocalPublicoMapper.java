package br.com.fiap.acessibilidade.dto;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import br.com.fiap.acessibilidade.model.LocalPublico;

@Component
public class LocalPublicoMapper {
    private final ModelMapper modelMapper = new ModelMapper();

    public LocalPublico toModel(LocalPublicoCreateRequest dto) {
        return modelMapper.map(dto, LocalPublico.class);
    }

    public LocalPublico toModel(Long id, LocalPublicoUpdateRequest dto) {
        LocalPublico local = modelMapper.map(dto, LocalPublico.class);
        local.setId(id);
        return local;
    }

    public LocalPublicoResponse toDto(LocalPublico entity) {
        return modelMapper.map(entity, LocalPublicoResponse.class);
    }
}
