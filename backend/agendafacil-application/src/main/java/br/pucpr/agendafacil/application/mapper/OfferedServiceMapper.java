package br.pucpr.agendafacil.application.mapper;

import br.pucpr.agendafacil.application.dto.CreateOfferedServiceRequest;
import br.pucpr.agendafacil.application.dto.OfferedServiceResponse;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.scheduling.pricing.PricingPolicyType;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Mapper responsável por converter serviços oferecidos entre entidade de
 * domínio e DTOs.
 */
@ApplicationScoped
public class OfferedServiceMapper {

    /**
     * Converte uma requisição de cadastro em serviço oferecido.
     *
     * <p>Quando a política de precificação não é informada, o serviço usa a
     * política fixa como padrão.</p>
     *
     * @param dto dados do serviço que será criado
     * @param business estabelecimento dono do serviço
     * @return serviço criado a partir da requisição
     */
    public OfferedService toEntity(CreateOfferedServiceRequest dto, Business business) {
        OfferedService s = new OfferedService(
                dto.name(),
                dto.basePrice(),
                dto.durationMinutes(),
                business
        );
        s.setDescription(dto.description());
        s.setPricingPolicyType(dto.pricingPolicyType() == null
                ? PricingPolicyType.FIXED : dto.pricingPolicyType());
        return s;
    }

    /**
     * Converte um serviço oferecido em sua representação de resposta.
     *
     * @param s serviço que será convertido
     * @return DTO com os dados principais do serviço
     */
    public OfferedServiceResponse toResponse(OfferedService s) {
        return new OfferedServiceResponse(
                s.getId(),
                s.getBusiness() == null ? null : s.getBusiness().getId(),
                s.getName(),
                s.getBasePrice(),
                s.getDurationMinutes(),
                s.getDescription(),
                s.getPricingPolicyType(),
                s.isActive()
        );
    }
}