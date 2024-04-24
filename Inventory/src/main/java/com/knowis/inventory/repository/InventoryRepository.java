package com.knowis.inventory.repository;

import com.knowis.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String> {
    // You can define custom query methods here if needed
}