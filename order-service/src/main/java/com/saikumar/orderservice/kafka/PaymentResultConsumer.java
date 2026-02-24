package com.saikumar.orderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saikumar.orderservice.model.Order;
import com.saikumar.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentResultConsumer {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void handlePaymentResult(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String eventType = (String) event.get("eventType");
            Long orderId = Long.valueOf(event.get("orderId").toString());

            orderRepository.findById(orderId).ifPresent(order -> {
                if ("PAYMENT_SUCCESS".equals(eventType)) {
                    order.setStatus(Order.OrderStatus.CONFIRMED);
                    log.info("Order {} confirmed after successful payment", orderId);
                } else if ("PAYMENT_FAILED".equals(eventType)) {
                    order.setStatus(Order.OrderStatus.PAYMENT_FAILED);
                    log.warn("Order {} payment failed", orderId);
                }
                orderRepository.save(order);
            });

        } catch (Exception e) {
            log.error("Error processing payment event: {}", message, e);
        }
    }
}
