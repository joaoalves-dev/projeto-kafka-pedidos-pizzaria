package br.com.pizzaria.orders.messaging;

import br.com.pizzaria.orders.domain.OrderStatus;
import br.com.pizzaria.orders.domain.PizzaOrder;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(UUID eventId, UUID orderId, String customerName, String customerPhone,
                                String deliveryAddress, BigDecimal totalAmount, OrderStatus status,
                                OffsetDateTime occurredAt, List<Item> items) {
    public record Item(String pizzaName, String size, Integer quantity, BigDecimal unitPrice) {}

    public static OrderCreatedEvent from(PizzaOrder order) {
        return new OrderCreatedEvent(UUID.randomUUID(), order.getId(), order.getCustomerName(),
            order.getCustomerPhone(), order.getDeliveryAddress(), order.getTotalAmount(), order.getStatus(),
            OffsetDateTime.now(), order.getItems().stream().map(i -> new Item(i.getPizzaName(), i.getSize(),
                i.getQuantity(), i.getUnitPrice())).toList());
    }
}
