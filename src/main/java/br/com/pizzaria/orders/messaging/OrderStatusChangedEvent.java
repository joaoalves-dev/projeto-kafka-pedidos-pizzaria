package br.com.pizzaria.orders.messaging;

import br.com.pizzaria.orders.domain.OrderStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderStatusChangedEvent(UUID eventId, UUID orderId, OrderStatus status, OffsetDateTime occurredAt) {}
