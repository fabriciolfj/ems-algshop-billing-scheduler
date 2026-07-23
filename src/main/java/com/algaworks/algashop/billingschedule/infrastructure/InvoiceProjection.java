package com.algaworks.algashop.billingschedule.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceProjection {
    private UUID id;
    private String paymentGatewayCode;
}
