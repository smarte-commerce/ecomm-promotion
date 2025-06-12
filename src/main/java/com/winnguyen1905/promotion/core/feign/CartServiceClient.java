package com.winnguyen1905.promotion.core.feign;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.promotion.core.model.request.CustomerCart;

@Service
@FeignClient(name = "CART-SERVICE", url = "http://localhost:8090")
public interface CartServiceClient {
  @GetMapping("/carts/{id}")
  ResponseEntity<CustomerCart> getCustomerCartDetail(@PathVariable UUID id);
}
