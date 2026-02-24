package com.saikumar.orderservice.controller;

import com.saikumar.orderservice.dto.OrderDto;
import com.saikumar.orderservice.model.Order;
import com.saikumar.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderDto.CreateOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(req));
    }

    // GET /api/orders/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // GET /api/orders/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    // GET /api/orders/user/{userId}/paged
    @GetMapping("/user/{userId}/paged")
    public ResponseEntity<Page<Order>> getOrdersByUserPaged(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getOrdersByUserPaged(userId, PageRequest.of(page, size)));
    }

    // GET /api/orders - admin only
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // PUT /api/orders/{id}/cancel
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    // PUT /api/orders/{id}/status - admin
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("order-service ok");
    }
}
