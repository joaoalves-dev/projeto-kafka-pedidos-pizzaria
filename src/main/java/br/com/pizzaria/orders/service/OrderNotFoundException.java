package br.com.pizzaria.orders.service;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(UUID id) { super("Pedido não encontrado: " + id); }
}
