package com.knowis.ordermanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderCreationRequest {

    private String productId;
    private int quantity;
    private String userId;
    private String cardTokenNo;
    private double amount;
    private String shippingAddress;
}
