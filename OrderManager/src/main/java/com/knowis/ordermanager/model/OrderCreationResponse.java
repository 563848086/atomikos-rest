package com.knowis.ordermanager.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OrderCreationResponse {

    private String orderId;
    private String status;
}
