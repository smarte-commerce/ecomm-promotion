package com.winnguyen1905.promotion.core.service;

import java.util.UUID;

import com.winnguyen1905.promotion.persistance.entity.EInventory;

public interface InventoryService {
  Boolean isAccessStock(EInventory inventory, Integer quantity);

  Boolean handleUpdateInventoryForReservation(UUID inventory, UUID customerId, Integer quantity);
}
