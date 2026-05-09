package br.pucpr.agendafacil.application.business;

import br.pucpr.agendafacil.application.dto.CreatePromotionRequest;
import br.pucpr.agendafacil.application.dto.PromotionResponse;
import br.pucpr.agendafacil.application.dto.UpdatePromotionRequest;
import br.pucpr.agendafacil.application.mapper.PromotionMapper;
import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.OfferedService;
import br.pucpr.agendafacil.domain.business.Promotion;
import br.pucpr.agendafacil.domain.business.port.BusinessRepository;
import br.pucpr.agendafacil.domain.business.port.OfferedServiceRepository;
import br.pucpr.agendafacil.domain.business.port.PromotionRepository;
import br.pucpr.agendafacil.shared.exception.BusinessRuleException;
import br.pucpr.agendafacil.shared.exception.ForbiddenException;
import br.pucpr.agendafacil.shared.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Serviço de aplicação responsável pela gestão de promoções.
 *
 * <p>Centraliza os casos de uso de listagem, consulta, criação, atualização,
 * desativação e reativação de promoções de um estabelecimento.</p>
 *
 * <p>Antes de alterar uma promoção, o serviço valida se o estabelecimento
 * pertence ao dono autenticado. Também garante que a promoção tenha apenas uma
 * forma de desconto: percentual ou valor fixo.</p>
 */
@ApplicationScoped
public class PromotionApplicationService {

    private final PromotionRepository promotionRepository;
    private final BusinessRepository businessRepository;
    private final OfferedServiceRepository offeredServiceRepository;
    private final PromotionMapper promotionMapper;

    @Inject
    public PromotionApplicationService(PromotionRepository promotionRepository,
                                       BusinessRepository businessRepository,
                                       OfferedServiceRepository offeredServiceRepository,
                                       PromotionMapper promotionMapper) {
        this.promotionRepository = promotionRepository;
        this.businessRepository = businessRepository;
        this.offeredServiceRepository = offeredServiceRepository;
        this.promotionMapper = promotionMapper;
    }

    /**
     * Lista as promoções de um estabelecimento do dono autenticado.
     *
     * @param ownerId identificador do dono autenticado
     * @param businessId identificador do estabelecimento
     * @return lista de promoções cadastradas para o estabelecimento
     * @throws ForbiddenException quando o estabelecimento não pertence ao dono
     * @throws NotFoundException quando o estabelecimento não existir
     */
    public List<PromotionResponse> listForBusiness(Long ownerId, Long businessId) {
        assertOwnership(ownerId, businessId);
        return promotionRepository.findByBusinessId(businessId).stream()
                .map(promotionMapper::toResponse)
                .toList();
    }

    /**
     * Busca uma promoção de um estabelecimento do dono autenticado.
     *
     * @param ownerId identificador do dono autenticado
     * @param businessId identificador do estabelecimento
     * @param promotionId identificador da promoção
     * @return dados da promoção encontrada
     * @throws ForbiddenException quando o estabelecimento não pertence ao dono
     * @throws NotFoundException quando a promoção não existir no estabelecimento
     */
    public PromotionResponse getById(Long ownerId, Long businessId, Long promotionId) {
        Promotion promotion = loadOrThrow(promotionId);
        assertPromotionBelongsTo(promotion, businessId);
        assertOwnership(ownerId, businessId);
        return promotionMapper.toResponse(promotion);
    }

    /**
     * Cria uma nova promoção para um estabelecimento do dono autenticado.
     *
     * <p>A promoção deve informar exatamente uma forma de desconto:
     * {@code discountPercentage} ou {@code discountAmount}. Quando
     * {@code eligibleServiceIds} vier nulo ou vazio, a promoção será aplicada a
     * todos os serviços do estabelecimento.</p>
     *
     * @param ownerId identificador do dono autenticado
     * @param businessId identificador do estabelecimento
     * @param req dados da promoção que será criada
     * @return promoção criada
     * @throws BusinessRuleException quando a regra de desconto ou o período forem inválidos
     * @throws ForbiddenException quando o estabelecimento ou algum serviço não pertencer ao dono
     * @throws NotFoundException quando o estabelecimento ou algum serviço não existir
     */
    @Transactional
    public PromotionResponse create(Long ownerId, Long businessId,
                                    CreatePromotionRequest req) {
        Business business = loadBusinessOrThrow(businessId);
        assertOwnership(ownerId, business);
        validateDiscount(req.discountPercentage(), req.discountAmount());
        validateDateRange(req.validFrom(), req.validTo());

        Promotion promotion = new Promotion(business, req.name(), req.validFrom(), req.validTo());
        promotion.setDescription(req.description());
        promotion.setDiscountPercentage(req.discountPercentage());
        promotion.setDiscountAmount(req.discountAmount());
        promotion.setEligibleServices(resolveServices(businessId, req.eligibleServiceIds()));
        promotionRepository.persist(promotion);
        return promotionMapper.toResponse(promotion);
    }

    /**
     * Atualiza uma promoção de um estabelecimento do dono autenticado.
     *
     * <p>A atualização substitui os dados principais da promoção, incluindo período,
     * desconto, serviços elegíveis e status de atividade.</p>
     *
     * @param ownerId identificador do dono autenticado
     * @param businessId identificador do estabelecimento
     * @param promotionId identificador da promoção
     * @param req novos dados da promoção
     * @return promoção atualizada
     * @throws BusinessRuleException quando a regra de desconto ou o período forem inválidos
     * @throws ForbiddenException quando o estabelecimento ou algum serviço não pertencer ao dono
     * @throws NotFoundException quando a promoção, estabelecimento ou algum serviço não existir
     */
    @Transactional
    public PromotionResponse update(Long ownerId, Long businessId, Long promotionId,
                                    UpdatePromotionRequest req) {
        Promotion promotion = loadOrThrow(promotionId);
        assertPromotionBelongsTo(promotion, businessId);
        assertOwnership(ownerId, businessId);
        validateDiscount(req.discountPercentage(), req.discountAmount());
        validateDateRange(req.validFrom(), req.validTo());

        promotion.setName(req.name());
        promotion.setDescription(req.description());
        promotion.setDiscountPercentage(req.discountPercentage());
        promotion.setDiscountAmount(req.discountAmount());
        promotion.setValidFrom(req.validFrom());
        promotion.setValidTo(req.validTo());
        promotion.setEligibleServices(resolveServices(businessId, req.eligibleServiceIds()));
        if (Boolean.TRUE.equals(req.active())) promotion.activate();
        else promotion.deactivate();

        Promotion saved = promotionRepository.update(promotion);
        return promotionMapper.toResponse(saved);
    }

    /**
     * Desativa uma promoção de um estabelecimento do dono autenticado.
     *
     * @param ownerId identificador do dono autenticado
     * @param businessId identificador do estabelecimento
     * @param promotionId identificador da promoção
     * @return promoção desativada
     * @throws ForbiddenException quando o estabelecimento não pertence ao dono
     * @throws NotFoundException quando a promoção não existir no estabelecimento
     */
    @Transactional
    public PromotionResponse deactivate(Long ownerId, Long businessId, Long promotionId) {
        Promotion promotion = loadOrThrow(promotionId);
        assertPromotionBelongsTo(promotion, businessId);
        assertOwnership(ownerId, businessId);
        promotion.deactivate();
        Promotion saved = promotionRepository.update(promotion);
        return promotionMapper.toResponse(saved);
    }

    /**
     * Reativa uma promoção de um estabelecimento do dono autenticado.
     *
     * @param ownerId identificador do dono autenticado
     * @param businessId identificador do estabelecimento
     * @param promotionId identificador da promoção
     * @return promoção reativada
     * @throws ForbiddenException quando o estabelecimento não pertence ao dono
     * @throws NotFoundException quando a promoção não existir no estabelecimento
     */
    @Transactional
    public PromotionResponse reactivate(Long ownerId, Long businessId, Long promotionId) {
        Promotion promotion = loadOrThrow(promotionId);
        assertPromotionBelongsTo(promotion, businessId);
        assertOwnership(ownerId, businessId);
        promotion.activate();
        Promotion saved = promotionRepository.update(promotion);
        return promotionMapper.toResponse(saved);
    }

    private Set<OfferedService> resolveServices(Long businessId, List<Long> ids) {
        Set<OfferedService> out = new HashSet<>();
        if (ids == null) return out;
        for (Long sid : ids) {
            OfferedService svc = offeredServiceRepository.getById(sid);
            if (svc == null) {
                throw new NotFoundException("Serviço não encontrado: id=" + sid);
            }
            if (svc.getBusiness() == null
                    || !Objects.equals(svc.getBusiness().getId(), businessId)) {
                throw new ForbiddenException(
                        "Serviço " + sid + " não pertence a este estabelecimento.");
            }
            out.add(svc);
        }
        return out;
    }

    private Promotion loadOrThrow(Long promotionId) {
        Promotion promotion = promotionRepository.getById(promotionId);
        if (promotion == null) {
            throw new NotFoundException("Promoção não encontrada.");
        }
        return promotion;
    }

    private Business loadBusinessOrThrow(Long businessId) {
        Business business = businessRepository.getById(businessId);
        if (business == null) {
            throw new NotFoundException("Estabelecimento não encontrado.");
        }
        return business;
    }

    private void assertOwnership(Long ownerId, Long businessId) {
        Business business = loadBusinessOrThrow(businessId);
        assertOwnership(ownerId, business);
    }

    private void assertOwnership(Long ownerId, Business business) {
        if (!Objects.equals(business.getOwner().getId(), ownerId)) {
            throw new ForbiddenException(
                    "Você não tem permissão para alterar este estabelecimento.");
        }
    }

    private void assertPromotionBelongsTo(Promotion promotion, Long businessId) {
        if (promotion.getBusiness() == null
                || !Objects.equals(promotion.getBusiness().getId(), businessId)) {
            throw new NotFoundException(
                    "Promoção não encontrada neste estabelecimento.");
        }
    }

    private void validateDiscount(BigDecimal pct, BigDecimal amount) {
        boolean hasPct = pct != null && pct.signum() > 0;
        boolean hasAmt = amount != null && amount.signum() > 0;
        if (hasPct == hasAmt) {
            throw new BusinessRuleException(
                    "Informe exatamente um entre desconto percentual e desconto fixo.");
        }
        if (hasPct && pct.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessRuleException(
                    "O desconto percentual não pode ser superior a 100%.");
        }
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new BusinessRuleException(
                    "A data final da promoção deve ser igual ou posterior à inicial.");
        }
    }
}