package br.com.pizzaria.orders.api;

import br.com.pizzaria.orders.domain.OrderStatus;
import br.com.pizzaria.orders.domain.PizzaOrder;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(UUID id, String customerName, String customerPhone, String deliveryAddress,
                            String notes, OrderStatus status, BigDecimal totalAmount,
                            OffsetDateTime createdAt, OffsetDateTime updatedAt, List<ItemResponse> items) {
    public record ItemResponse(String pizzaName, String size, Integer quantity, BigDecimal unitPrice,
                               BigDecimal subtotal) {}

    public static OrderResponse from(PizzaOrder order) {
        return new OrderResponse(order.getId(), order.getCustomerName(), order.getCustomerPhone(),
            order.getDeliveryAddress(), order.getNotes(), order.getStatus(), order.getTotalAmount(),
            order.getCreatedAt(), order.getUpdatedAt(),
            order.getItems().stream().map(i -> new ItemResponse(i.getPizzaName(), i.getSize(),
                i.getQuantity(), i.getUnitPrice(), i.subtotal())).toList());
    }
}
