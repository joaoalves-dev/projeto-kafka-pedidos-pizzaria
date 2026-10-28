package br.com.pizzaria.orders.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pizza_name", nullable = false, length = 100)
    private String pizzaName;
    @Column(nullable = false, length = 20)
    private String size;
    @Column(nullable = false)
    private Integer quantity;
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    protected OrderItem() {}

    public OrderItem(String pizzaName, String size, Integer quantity, BigDecimal unitPrice) {
        this.pizzaName = pizzaName;
        this.size = size;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal subtotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
    public Long getId() { return id; }
    public String getPizzaName() { return pizzaName; }
    public String getSize() { return size; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}
