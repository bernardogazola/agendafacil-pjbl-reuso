package br.pucpr.agendafacil.application.mapper;

import br.pucpr.agendafacil.application.dto.PromotionResponse;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.business.Promotion;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Mapper responsável por converter promoções para DTOs de resposta.
 */
@ApplicationScoped
public class PromotionMapper {

    /**
     * Converte uma promoção em sua representação de resposta.
     *
     * <p>Quando a promoção não possui serviços elegíveis específicos, a lista de
     * ids é retornada vazia, indicando que a promoção vale para todos os
     * serviços do estabelecimento.</p>
     *
     * @param p promoção que será convertida
     * @return DTO com os dados principais da promoção
     */
    public PromotionResponse toResponse(Promotion p) {
        Set<OfferedService> services = p.getEligibleServices();
        List<Long> ids = services == null
                ? List.of()
                : services.stream()
                  .map(OfferedService::getId)
                  .filter(Objects::nonNull)
                  .toList();

        return new PromotionResponse(
                p.getId(),
                p.getBusiness() == null ? null : p.getBusiness().getId(),
                p.getName(),
                p.getDescription(),
                p.getDiscountPercentage(),
                p.getDiscountAmount(),
                p.getValidFrom(),
                p.getValidTo(),
                ids,
                p.isActive()
        );
    }
}