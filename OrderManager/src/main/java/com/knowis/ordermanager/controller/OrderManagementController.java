package com.knowis.ordermanager.controller;


import com.knowis.ordermanager.model.OrderCreationRequest;
import com.knowis.ordermanager.model.OrderCreationResponse;
import com.knowis.ordermanager.model.ProcessStepResponse;
import com.knowis.ordermanager.service.OrderManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderManagementController {

    private final OrderManagementService orderManagementService;

    public OrderManagementController(OrderManagementService orderManagementService) {

        this.orderManagementService = orderManagementService;
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<?> getOrderStatus(@PathVariable String orderId){
        String orderStatus =  orderManagementService.getOrderStatus(orderId);
        if(Optional.ofNullable(orderStatus).isPresent()){
            return ResponseEntity.ok(OrderCreationResponse.builder()
                    .orderId(orderId)
                    .status(orderStatus).build());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ProcessStepResponse.builder()
                            .status(HttpStatus.NOT_FOUND.name())
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .errorMsg("Unable to find Order with id : "+orderId)
                            .successMsg(null).build()
                    );
        }
    }

    @PostMapping("/create")
    public ResponseEntity<OrderCreationResponse> createOrder(@RequestBody OrderCreationRequest ocr) throws Exception {


        OrderCreationResponse orderCreationResponse =  orderManagementService.createOrder(ocr);

        return ResponseEntity.ok(orderCreationResponse);

    }

}
