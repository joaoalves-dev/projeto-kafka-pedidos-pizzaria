package br.com.pizzaria.orders.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class PizzaOrder {
    @Id
    private UUID id;

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;
    @Column(name = "customer_phone", nullable = false, length = 30)
    private String customerPhone;
    @Column(name = "delivery_address", nullable = false, length = 300)
    private String deliveryAddress;
    @Column(length = 500)
    private String notes;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    private List<OrderItem> items = new ArrayList<>();

    protected PizzaOrder() {}

    public static PizzaOrder create(String customerName, String customerPhone, String deliveryAddress,
                                    String notes, List<OrderItem> items) {
        PizzaOrder order = new PizzaOrder();
        order.id = UUID.randomUUID();
        order.customerName = customerName;
        order.customerPhone = customerPhone;
        order.deliveryAddress = deliveryAddress;
        order.notes = notes;
        order.status = OrderStatus.RECEIVED;
        order.items = new ArrayList<>(items);
        order.totalAmount = items.stream().map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.createdAt = OffsetDateTime.now();
        order.updatedAt = order.createdAt;
        return order;
    }

    public void updateStatus(OrderStatus newStatus) {
        if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Pedido finalizado não pode ter o status alterado.");
        }
        if (newStatus == status) return;
        this.status = newStatus;
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getNotes() { return notes; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
}
