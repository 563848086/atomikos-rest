package com.knowis.inventory.model;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ProcessStepResponse {

    private String status;
    private int statusCode;
    private String successMsg;
    private String errorMsg;
}
