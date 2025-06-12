package com.winnguyen1905.promotion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class PromotionApplication {
  
  public static void main(String[] args) {
    SpringApplication.run(PromotionApplication.class, args);
  }

}
