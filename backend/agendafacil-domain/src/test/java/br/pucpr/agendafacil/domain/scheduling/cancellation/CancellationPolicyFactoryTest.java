package br.pucpr.agendafacil.domain.scheduling.cancellation;

import br.pucpr.agendafacil.domain.business.Business;
import br.pucpr.agendafacil.domain.business.BusinessCategory;
import br.pucpr.agendafacil.domain.business.BusinessPlan;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CancellationPolicyFactoryTest {

    private final CancellationPolicyFactory factory = new CancellationPolicyFactory();

    @Test
    void free_resolvesToFreeCancellation() {
        assertInstanceOf(FreeCancellation.class, factory.resolve(business(CancellationPolicyType.FREE)));
    }

    @Test
    void deadline_resolvesToDeadlineCancellation() {
        assertInstanceOf(DeadlineCancellation.class, factory.resolve(business(CancellationPolicyType.DEADLINE)));
    }

    @Test
    void feeBased_resolvesToFeeBasedCancellation() {
        assertInstanceOf(FeeBasedCancellation.class, factory.resolve(business(CancellationPolicyType.FEE_BASED)));
    }

    private static Business business(CancellationPolicyType type) {
        Business b = new Business("Negócio", "abc@a.com",
                BusinessCategory.BARBER_SHOP, BusinessPlan.BASIC, null);
        b.setCancellationPolicyType(type);
        return b;
    }
}