package com.knowis.payment.service;

import com.knowis.payment.entity.Payment;
import com.knowis.payment.model.PaymentRequest;
import com.knowis.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }


    public Payment createPayment(PaymentRequest paymentRequest) {
        Payment payment = new Payment();
        String paymentId = UUID.randomUUID().toString();
        payment.setPaymentId(paymentId);
        payment.setUserId(paymentRequest.getUserId());
        payment.setProductId(paymentRequest.getProductId());
        payment.setOrderId(paymentRequest.getOrderId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setCardToken(paymentRequest.getCardToken());

//        Optional<Payment> payment2Optional = paymentRepository.findById(paymentId);
//
//        log.info("Payment found?  "+payment2Optional.isPresent());
//        payment2Optional.ifPresent(value -> log.info("Payment retrieved : " + value.toString()));

        return paymentRepository.save(payment);
    }



}
