package com.knowis.ordermanager.service;

import com.knowis.ordermanager.model.OrderCreationRequest;
import com.knowis.ordermanager.model.PaymentRequest;
import com.knowis.ordermanager.model.ProcessStepResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class OrderProcessingService {


    @Value("${service.payment.url}")
    private String PAYMENT_SERVICE_URL;

    @Value("${service.inventory.url}")
    private String INVENTORY_SERVICE_URL;

    private final RestTemplate restTemplate;

    public OrderProcessingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void processOrder(String orderId, OrderCreationRequest ocr) throws Exception{
        pay(orderId, ocr);
        updateInventory(ocr);
    }

    private void pay(String orderId, OrderCreationRequest ocr)  {

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(orderId)
                .amount(ocr.getAmount())
                .cardToken(ocr.getCardTokenNo())
                .userId(ocr.getUserId())
                .productId(ocr.getProductId())
                .build();
        HttpEntity<PaymentRequest> request = new HttpEntity<>(paymentRequest, getHeaders());

        String url = String.format("%s/payment/pay", PAYMENT_SERVICE_URL);
        log.info(url);
        ResponseEntity<ProcessStepResponse> response = restTemplate.postForEntity(
                url, request, ProcessStepResponse.class);

        if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
            throw new RuntimeException("Failed to process payment,  Status Code : "+response.getStatusCode());
        }
    }

    private void updateInventory(OrderCreationRequest ocr)  {


            HttpEntity<?> request = new HttpEntity<>(getHeaders());
            String url = String.format("%s/inventory/product/%s/quantity/%s",
                    INVENTORY_SERVICE_URL, ocr.getProductId(), ocr.getQuantity());

            log.info(url);
            ResponseEntity<ProcessStepResponse> response = restTemplate.postForEntity(
                    url, request, ProcessStepResponse.class);

            if (response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
                throw new RuntimeException("Failed to update inventory , Status Code : "+response.getStatusCode());
            }
    }

    private HttpHeaders getHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
