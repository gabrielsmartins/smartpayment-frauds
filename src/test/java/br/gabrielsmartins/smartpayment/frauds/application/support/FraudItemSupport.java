package br.gabrielsmartins.smartpayment.frauds.application.support;

import br.gabrielsmartins.smartpayment.frauds.application.domain.FraudItem;

import java.math.BigDecimal;
import java.util.UUID;

public class FraudItemSupport {

    private FraudItemSupport(){
        super();
    }

    public static FraudItem.FraudItemBuilder defaultFraudItem(){
        return FraudItem.builder()
                        .withProductId(UUID.randomUUID())
                        .withQuantity(1)
                        .withAmount(BigDecimal.valueOf(500));
    }
}
