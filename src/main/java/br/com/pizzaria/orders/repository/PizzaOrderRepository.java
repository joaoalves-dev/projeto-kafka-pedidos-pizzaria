package br.com.pizzaria.orders.repository;

import br.com.pizzaria.orders.domain.OrderStatus;
import br.com.pizzaria.orders.domain.PizzaOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import java.util.List;
import java.util.UUID;

public interface PizzaOrderRepository extends JpaRepository<PizzaOrder, UUID> {
    List<PizzaOrder> findAllByStatusOrderByCreatedAtDesc(OrderStatus status);

    @Query("""
    SELECT DISTINCT o
    FROM PizzaOrder o
    LEFT JOIN FETCH o.items
    WHERE o.status = :status
    ORDER BY o.createdAt DESC
    """)
    List<PizzaOrder> findAllByStatusWithItems(@Param("status") OrderStatus status);

    @Query("SELECT DISTINCT o FROM PizzaOrder o LEFT JOIN FETCH o.items")
    List<PizzaOrder> findAllWithItems();

    @Query("""
    SELECT o
    FROM PizzaOrder o
    LEFT JOIN FETCH o.items
    WHERE o.id = :id
    """)
    Optional<PizzaOrder> findByIdWithItems(@Param("id") UUID id);
}
