package com.saikumar.orderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public static final String TOPIC = "order-events";

    public void sendOrderCreated(Long orderId, Long userId, Double totalAmount) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "ORDER_CREATED");
        event.put("orderId", orderId);
        event.put("userId", userId);
        event.put("amount", totalAmount);

        publish(orderId.toString(), event);
    }

    public void sendOrderCancelled(Long orderId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "ORDER_CANCELLED");
        event.put("orderId", orderId);

        publish(orderId.toString(), event);
    }

    private void publish(String key, Map<String, Object> event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, key, json);
            log.info("Event sent to {}: {}", TOPIC, json);
        } catch (Exception e) {
            log.error("Failed to send Kafka event for key {}", key, e);
        }
    }
}
