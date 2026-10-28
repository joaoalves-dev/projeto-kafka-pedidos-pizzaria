package br.com.pizzaria.orders.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {
    private static final Logger log = LoggerFactory.getLogger(OrderCreatedConsumer.class);

    @KafkaListener(topics = "${app.kafka.topics.order-created}", groupId = "pizzaria-kitchen")
    public void onOrderCreated(OrderCreatedEvent event) {
        // Exemplo de consumidor de cozinha. Em produção, substituir por integração com o serviço de cozinha.
        log.info("Novo pedido recebido pela cozinha: orderId={}, cliente={}, total={}, itens={}",
                event.orderId(), event.customerName(), event.totalAmount(), event.items().size());
    }
}
