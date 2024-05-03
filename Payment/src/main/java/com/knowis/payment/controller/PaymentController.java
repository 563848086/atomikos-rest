package com.knowis.payment.controller;


import com.knowis.payment.model.PaymentDTO;
import com.knowis.payment.model.PaymentRequest;
import com.knowis.payment.model.ProcessStepResponse;
import com.knowis.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    @GetMapping("/health")
    public ResponseEntity<String> healthCheck(){
        return ResponseEntity.ok("Payment app is Running! (version 1.0)");
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentDTO>> getAllPayments(){
        return ResponseEntity.ok(this.paymentService.getAllPayments());
    }


    @PostMapping("/pay")
    public ResponseEntity<ProcessStepResponse> pay(@RequestBody PaymentRequest paymentRequest) {
        log.info("Processing Payment Request");
        ProcessStepResponse processStepResponse;
        try {
            paymentService.createPayment(paymentRequest);

            processStepResponse = ProcessStepResponse.builder()
                     .errorMsg(null)
                     .statusCode(HttpStatus.CREATED.value())
                     .successMsg("Payment Success!")
                     .status("SUCCESS")
                     .build();
            return ResponseEntity.ok(processStepResponse);


        } catch (Exception e) {
            log.error(String.format("Error in creating payment : %s",e.getMessage()));
            processStepResponse = ProcessStepResponse.builder()
                    .errorMsg(e.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .successMsg(null)
                    .status("FAILED")
                    .build();
            return ResponseEntity.internalServerError().body(processStepResponse);

        }

    }

}
