package com.knowis.inventory.controller;


import com.knowis.inventory.entity.Inventory;
import com.knowis.inventory.model.ProcessStepResponse;
import com.knowis.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }



    @PostMapping("/product/{productId}/quantity/{quantity}")
    public ResponseEntity<ProcessStepResponse> updateInventory(@PathVariable String productId, @PathVariable String quantity)  {
        log.info("Updating Inventory");
        ProcessStepResponse processStepResponse;
        Inventory inventory;
        int requestedQuantity = Integer.parseInt(quantity);
        try {
            inventory = inventoryService.getProductDetails(productId);

            if(inventory.getAvailableQuantity() > requestedQuantity){
                inventoryService.update(inventory, inventory.getAvailableQuantity() - requestedQuantity);
                processStepResponse = ProcessStepResponse.builder()
                        .status("SUCCESS")
                        .statusCode(HttpStatus.OK.value())
                        .errorMsg(null)
                        .successMsg("Inventory updated!")
                        .build();
                return ResponseEntity.ok(processStepResponse);
            } else {
                processStepResponse = ProcessStepResponse.builder()
                        .status("FAILED")
                        .statusCode(HttpStatus.GONE.value())
                        .errorMsg("Product "+productId+" currently unavailable")
                        .successMsg(null)
                        .build();
                return ResponseEntity.status(HttpStatus.GONE).body(processStepResponse);
            }
        } catch (Exception e) {
            processStepResponse = ProcessStepResponse.builder()
                    .status("FAILED")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .errorMsg(e.getMessage())
                    .successMsg(null)
                    .build();
            return ResponseEntity.internalServerError().body(processStepResponse);
        }


    }

}
