package com.knowis.payment.model;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PaymentDTO {

    private String paymentId;
    private String orderId;
    private String userId;
    private String productId;
    private String cardToken;
    private double amount;
}
