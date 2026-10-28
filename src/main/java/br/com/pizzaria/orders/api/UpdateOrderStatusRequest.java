package br.com.pizzaria.orders.api;

import br.com.pizzaria.orders.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {}
