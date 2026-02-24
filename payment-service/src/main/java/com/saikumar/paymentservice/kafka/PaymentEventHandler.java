package com.saikumar.paymentservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saikumar.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventHandler {

    private final PaymentService paymentService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public static final String PAYMENT_TOPIC = "payment-events";

    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    public void handleOrderEvent(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String type = (String) event.get("eventType");

            if ("ORDER_CREATED".equals(type)) {
                Long orderId = Long.valueOf(event.get("orderId").toString());
                Long userId = Long.valueOf(event.get("userId").toString());
                Double amount = Double.valueOf(event.get("amount").toString());

                boolean success = paymentService.processPayment(orderId, userId, amount);
                publishResult(orderId, success);
            }

        } catch (Exception e) {
            log.error("Error handling order event: {}", message, e);
        }
    }

    private void publishResult(Long orderId, boolean success) {
        Map<String, Object> result = new HashMap<>();
        result.put("eventType", success ? "PAYMENT_SUCCESS" : "PAYMENT_FAILED");
        result.put("orderId", orderId);

        try {
            String json = objectMapper.writeValueAsString(result);
            kafkaTemplate.send(PAYMENT_TOPIC, orderId.toString(), json);
            log.info("Payment result sent for order {}: {}", orderId, success ? "SUCCESS" : "FAILED");
        } catch (Exception e) {
            log.error("Failed to publish payment result for order {}", orderId, e);
        }
    }
}
