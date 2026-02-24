package com.saikumar.orderservice.service;

import com.saikumar.orderservice.dto.OrderDto;
import com.saikumar.orderservice.exception.OrderNotFoundException;
import com.saikumar.orderservice.kafka.OrderEventProducer;
import com.saikumar.orderservice.model.Order;
import com.saikumar.orderservice.model.OrderItem;
import com.saikumar.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;
    private final WebClient.Builder webClientBuilder;

    @Transactional
    public Order createOrder(OrderDto.CreateOrderRequest req) {
        Order order = new Order();
        order.setUserId(req.getUserId());
        order.setShippingAddress(req.getShippingAddress());
        order.setNotes(req.getNotes());
        order.setStatus(Order.OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderDto.OrderItemRequest itemReq : req.getItems()) {
            // fetch product info from product-service
            Map productData = webClientBuilder.build()
                    .get()
                    .uri("http://product-service/api/products/" + itemReq.getProductId())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (productData == null) {
                throw new RuntimeException("Product not found: " + itemReq.getProductId());
            }

            // reduce stock - product service handles the check
            webClientBuilder.build()
                    .put()
                    .uri("http://product-service/api/products/" + itemReq.getProductId()
                            + "/stock/reduce?quantity=" + itemReq.getQuantity())
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            String productName = (String) productData.get("name");
            BigDecimal price = new BigDecimal(productData.get("price").toString());

            OrderItem item = new OrderItem(
                    itemReq.getProductId(),
                    productName,
                    itemReq.getQuantity(),
                    price
            );
            item.setOrder(order);
            order.getItems().add(item);
            total = total.add(item.getSubtotal());
        }

        order.setTotalAmount(total);
        order.setStatus(Order.OrderStatus.PAYMENT_PROCESSING);
        Order saved = orderRepository.save(order);

        // fire and forget to payment service via Kafka
        eventProducer.sendOrderCreated(saved.getId(), saved.getUserId(), saved.getTotalAmount().doubleValue());
        log.info("Order {} created for user {}, total: {}", saved.getId(), saved.getUserId(), saved.getTotalAmount());

        return saved;
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Page<Order> getOrdersByUserPaged(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Order cancelOrder(Long id) {
        Order order = getOrderById(id);

        if (order.getStatus() == Order.OrderStatus.SHIPPED ||
                order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel an order that has already been shipped or delivered");
        }

        // restore stock for each item
        for (OrderItem item : order.getItems()) {
            webClientBuilder.build()
                    .put()
                    .uri("http://product-service/api/products/" + item.getProductId()
                            + "/stock/restore?quantity=" + item.getQuantity())
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
        eventProducer.sendOrderCancelled(id);

        log.info("Order {} cancelled", id);
        return order;
    }

    public Order updateStatus(Long id, Order.OrderStatus status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
