package br.gabrielsmartins.smartpayment.frauds.adapters.persistence.entity;

import br.gabrielsmartins.smartpayment.frauds.adapters.persistence.entity.enums.PaymentMethodData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class FraudEntityTest {

    @Test
    @DisplayName("Given Item When Add Then Return Items Size")
    public void givenItemWhenAddThenReturnItemsSize(){
        FraudEntity fraudEntity = new FraudEntity();
        FraudItemEntity itemEntity = FraudItemEntity.builder()
                                                     .withId(FraudItemEntity.FraudItemEntityId.builder()
                                                             .build())
                                                     .build();
        Integer itemSize = fraudEntity.addItem(itemEntity);
        assertThat(itemSize).isEqualTo(1);
        assertThat(itemEntity.getId().getFraud()).isNotNull();
    }

    @Test
    @DisplayName("Given Payment Set When Add Then Return PaymentSets Size")
    public void givenPaymentSetWhenAddThenReturnPaymentSetsSize(){
        FraudEntity fraudEntity = new FraudEntity();
        Integer paymentSetsSize = fraudEntity.addPaymentMethod(PaymentMethodData.CASH, BigDecimal.valueOf(500));
        assertThat(paymentSetsSize).isEqualTo(1);
    }
}
