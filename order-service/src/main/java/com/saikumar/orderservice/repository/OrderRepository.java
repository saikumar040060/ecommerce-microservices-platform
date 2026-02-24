package com.saikumar.orderservice.repository;

import com.saikumar.orderservice.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    List<Order> findByStatus(Order.OrderStatus status);
}
