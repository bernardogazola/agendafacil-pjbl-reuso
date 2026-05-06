package br.pucpr.agendafacil.application.mapper;

import br.pucpr.agendafacil.application.dto.BusinessResponse;
import br.pucpr.agendafacil.domain.business.Business;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper responsável por converter estabelecimentos para DTOs de resposta.
 */
@ApplicationScoped
public class BusinessMapper {

    /**
     * Converte um estabelecimento em sua representação de resposta.
     *
     * @param b estabelecimento que será convertido
     * @return DTO com os dados principais do estabelecimento
     */
    public BusinessResponse toResponse(Business b) {
        return new BusinessResponse(
                b.getId(),
                b.getTradeName(),
                b.getEmail(),
                b.getPhone(),
                b.getCategory(),
                b.getPlan(),
                b.getCancellationPolicyType(),
                b.isActive()
        );
    }
}