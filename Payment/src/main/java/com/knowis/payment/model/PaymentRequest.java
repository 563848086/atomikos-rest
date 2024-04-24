package com.knowis.payment.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PaymentRequest {

    private String orderId;
    private String productId;
    private String userId;
    private String cardToken;
    private double amount;

}
