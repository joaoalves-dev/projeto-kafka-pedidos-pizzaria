package br.com.pizzaria.orders.service;

import br.com.pizzaria.orders.api.CreateOrderRequest;
import br.com.pizzaria.orders.domain.OrderItem;
import br.com.pizzaria.orders.domain.OrderStatus;
import br.com.pizzaria.orders.domain.PizzaOrder;
import br.com.pizzaria.orders.messaging.OrderEventPublisher;
import br.com.pizzaria.orders.messaging.OrderStatusChangedEvent;
import br.com.pizzaria.orders.repository.PizzaOrderRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final PizzaOrderRepository repository;
    private final OrderEventPublisher publisher;

    public OrderService(PizzaOrderRepository repository, OrderEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Transactional
    public PizzaOrder create(CreateOrderRequest request) {
        List<OrderItem> items = request.items().stream()
            .map(i -> new OrderItem(i.pizzaName(), i.size(), i.quantity(), i.unitPrice())).toList();
        PizzaOrder order = repository.save(PizzaOrder.create(request.customerName(), request.customerPhone(),
            request.deliveryAddress(), request.notes(), items));
        // Para alta confiabilidade entre DB e Kafka, usar Outbox Pattern (documentado no README).
        publisher.publishCreated(order);
        return order;
    }

    @Transactional(readOnly = true)
    public PizzaOrder get(UUID id) {
        return repository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<PizzaOrder> list(OrderStatus status) {
        return status == null
                ? repository.findAllWithItems()
                : repository.findAllByStatusWithItems(status);
    }

    @Transactional
    public PizzaOrder updateStatus(UUID id, OrderStatus status) {
        PizzaOrder order = repository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        OrderStatus previous = order.getStatus();
        order.updateStatus(status);

        PizzaOrder saved = repository.save(order);

        if (previous != saved.getStatus()) {
            publisher.publishStatusChanged(
                    new OrderStatusChangedEvent(
                            UUID.randomUUID(),
                            id,
                            status,
                            OffsetDateTime.now()
                    )
            );
        }

        return saved;
    }
}
