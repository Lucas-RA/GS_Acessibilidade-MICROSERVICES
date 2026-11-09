package br.com.fiap.acessibilidade.dto;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import br.com.fiap.acessibilidade.model.RelatoAcessibilidade;

@Component
public class RelatoAcessibilidadeMapper {
    private final ModelMapper modelMapper = new ModelMapper();

    // Aqui não dá para usar o modelMapper.map: o setId() da entidade casa com usuarioId e com localId
    // e o ModelMapper acusa ambiguidade. Usuário e local são buscados e setados no Service.
    public RelatoAcessibilidade toModel(RelatoAcessibilidadeCreateRequest dto) {
        RelatoAcessibilidade relato = new RelatoAcessibilidade();
        relato.setDescricao(dto.getDescricao());
        relato.setCategoria(dto.getCategoria());
        relato.setFotoUrl(dto.getFotoUrl());
        relato.setDescricaoFoto(dto.getDescricaoFoto());
        return relato;
    }

    public RelatoAcessibilidade toModel(Long id, RelatoAcessibilidadeUpdateRequest dto) {
        RelatoAcessibilidade relato = modelMapper.map(dto, RelatoAcessibilidade.class);
        relato.setId(id);
        return relato;
    }

    // usuarioId, usuarioNome, localId e localNome saem de usuario.id, usuario.nome, local.id e local.nome
    public RelatoAcessibilidadeResponse toDto(RelatoAcessibilidade entity) {
        return modelMapper.map(entity, RelatoAcessibilidadeResponse.class);
    }
}
