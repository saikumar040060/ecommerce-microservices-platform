package com.saikumar.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

public class OrderDto {

    @Data
    public static class CreateOrderRequest {
        @NotNull
        private Long userId;

        @NotEmpty(message = "Order must have at least one item")
        private List<OrderItemRequest> items;

        private String shippingAddress;
        private String notes;
    }

    @Data
    public static class OrderItemRequest {
        @NotNull
        private Long productId;

        @NotNull
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }
}
