package br.com.pizzaria.orders.messaging;

import br.com.pizzaria.orders.domain.PizzaOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String createdTopic;
    private final String statusTopic;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.order-created}") String createdTopic,
            @Value("${app.kafka.topics.order-status}") String statusTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.createdTopic = createdTopic;
        this.statusTopic = statusTopic;
    }

    public void publishCreated(PizzaOrder order) {
        kafkaTemplate.send(createdTopic, order.getId().toString(), OrderCreatedEvent.from(order));
    }

    public void publishStatusChanged(OrderStatusChangedEvent event) {
        kafkaTemplate.send(statusTopic, event.orderId().toString(), event);
    }
}
