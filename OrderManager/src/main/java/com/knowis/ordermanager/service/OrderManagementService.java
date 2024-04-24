package com.knowis.ordermanager.service;

import com.knowis.ordermanager.entity.Order;
import com.knowis.ordermanager.model.OrderCreationRequest;
import com.knowis.ordermanager.model.OrderCreationResponse;
import com.knowis.ordermanager.model.OrderStatus;
import com.knowis.ordermanager.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class OrderManagementService {
    private final OrderRepository orderRepository;
    private final OrderProcessingService orderProcessingService;
    private final Executor taskExecutor; // Executor for running CompletableFuture tasks

    @Autowired
    public OrderManagementService(OrderRepository orderRepository, OrderProcessingService orderProcessingService,
                                  @Qualifier("taskExecutor") Executor taskExecutor) {

        this.orderRepository = orderRepository;
        this.orderProcessingService = orderProcessingService;
        this.taskExecutor = taskExecutor;
    }

    public OrderCreationResponse createOrder(OrderCreationRequest ocr){

        String orderId = saveOrder(ocr, OrderStatus.IN_PROGRESS.name());

        //Start order processing in separate thread asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                orderProcessingService.processOrder(orderId, ocr);
                updateOrderStatus(orderId, OrderStatus.COMPLETED.name());
            } catch (Exception e) {
                log.error("Failed to process order " +
                         e.getMessage());
                updateOrderStatus(orderId, OrderStatus.FAILED.name());
            }
        }, taskExecutor);



        return OrderCreationResponse.builder()
                .orderId(orderId)
                .status(OrderStatus.IN_PROGRESS.name())
                .build();
    }

    private String saveOrder(OrderCreationRequest ocr, String status ){
        String orderId = UUID.randomUUID().toString();
        Order order = Order.builder()
                .orderId(orderId)
                .productId(ocr.getProductId())
                .userId(ocr.getUserId())
                .status(status)
                .build();
        orderRepository.save(order);
        return orderId;
    }

    public void updateOrderStatus(String orderId , String status){
        Optional<Order> orderOptional =  orderRepository.findById(orderId);
        Order order = orderOptional.get();
        order.setStatus(status);
        orderRepository.save(order);
    }


    public String getOrderStatus(String orderId){
        Optional<Order> orderOptional =  orderRepository.findById(orderId);
        return orderOptional.map(Order::getStatus).orElse(null);
    }
}
