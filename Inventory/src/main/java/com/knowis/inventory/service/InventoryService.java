package com.knowis.inventory.service;

import com.knowis.inventory.entity.Inventory;
import com.knowis.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;



    public InventoryService(InventoryRepository inventoryRepository) {

        this.inventoryRepository = inventoryRepository;
    }


    public Inventory getProductDetails(String productId){
        Optional<Inventory> inventoryOptional = inventoryRepository.findById(productId);
        if(inventoryOptional.isPresent()){
            return inventoryOptional.get();
        } else {
            throw new RuntimeException("Product not found in Inventory");
        }
    }
    public void update(Inventory inventory, int updatedQuantity) throws Exception {

        inventory.setAvailableQuantity(updatedQuantity);
        inventoryRepository.save(inventory);

    }
}
