package com.algaworks.algashop.billingschedule.infrastructure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Data
@Validated
@ConfigurationProperties(prefix = "algashop.integrations.payment")
public class AlgaShopPaymentProperties {

    @NotNull
    private FastpayProperties fastpay;


    @Validated
    @Data
    public static class FastpayProperties {
        @NotBlank
        private String hostname;

        @NotBlank
        private String privateToken;
    }
}
