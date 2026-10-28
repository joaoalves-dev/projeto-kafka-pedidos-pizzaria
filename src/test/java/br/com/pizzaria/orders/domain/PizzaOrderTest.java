package br.com.pizzaria.orders.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PizzaOrderTest {
    @Test
    void shouldCalculateOrderTotal() {
        PizzaOrder order = PizzaOrder.create("Ana", "11999999999", "Rua A, 10", null, List.of(
            new OrderItem("Calabresa", "GRANDE", 2, new BigDecimal("49.90")),
            new OrderItem("Refrigerante", "MEDIA", 1, new BigDecimal("8.00"))
        ));
        assertEquals(new BigDecimal("107.80"), order.getTotalAmount());
        assertEquals(OrderStatus.RECEIVED, order.getStatus());
    }

    @Test
    void shouldNotChangeStatusAfterDelivered() {
        PizzaOrder order = PizzaOrder.create("Ana", "11999999999", "Rua A, 10", null,
            List.of(new OrderItem("Mussarela", "MEDIA", 1, new BigDecimal("35.00"))));
        order.updateStatus(OrderStatus.DELIVERED);
        assertThrows(IllegalStateException.class, () -> order.updateStatus(OrderStatus.PREPARING));
    }
}
