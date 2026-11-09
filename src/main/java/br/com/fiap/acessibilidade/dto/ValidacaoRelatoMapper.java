package br.com.fiap.acessibilidade.dto;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import br.com.fiap.acessibilidade.model.ValidacaoRelato;

@Component
public class ValidacaoRelatoMapper {
    private final ModelMapper modelMapper = new ModelMapper();

    // Mesmo caso do relato: o setId() da entidade casa com relatoId e com usuarioId.
    // Relato e usuário são buscados e setados no Service.
    public ValidacaoRelato toModel(ValidacaoRelatoCreateRequest dto) {
        ValidacaoRelato validacao = new ValidacaoRelato();
        validacao.setSituacao(dto.getSituacao());
        validacao.setComentario(dto.getComentario());
        return validacao;
    }

    public ValidacaoRelato toModel(Long id, ValidacaoRelatoUpdateRequest dto) {
        ValidacaoRelato validacao = modelMapper.map(dto, ValidacaoRelato.class);
        validacao.setId(id);
        return validacao;
    }

    // relatoId, usuarioId e usuarioNome saem de relato.id, usuario.id e usuario.nome
    public ValidacaoRelatoResponse toDto(ValidacaoRelato entity) {
        return modelMapper.map(entity, ValidacaoRelatoResponse.class);
    }
}
