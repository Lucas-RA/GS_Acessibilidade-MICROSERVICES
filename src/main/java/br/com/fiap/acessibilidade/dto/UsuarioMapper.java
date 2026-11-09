package br.com.fiap.acessibilidade.dto;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import br.com.fiap.acessibilidade.model.Usuario;

@Component
public class UsuarioMapper {
    private final ModelMapper modelMapper = new ModelMapper();

    public Usuario toModel(UsuarioCreateRequest dto) {
        return modelMapper.map(dto, Usuario.class);
    }

    public Usuario toModel(Long id, UsuarioUpdateRequest dto) {
        Usuario usuario = modelMapper.map(dto, Usuario.class);
        usuario.setId(id);
        return usuario;
    }

    public UsuarioResponse toDto(Usuario entity) {
        return modelMapper.map(entity, UsuarioResponse.class);
    }
}
