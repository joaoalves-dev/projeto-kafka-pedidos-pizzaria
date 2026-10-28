package br.com.pizzaria.orders.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
    @NotBlank @Size(max = 120) String customerName,
    @NotBlank @Size(max = 30) String customerPhone,
    @NotBlank @Size(max = 300) String deliveryAddress,
    @Size(max = 500) String notes,
    @NotEmpty @Size(max = 30) List<@Valid OrderItemRequest> items
) {
    public record OrderItemRequest(
        @NotBlank @Size(max = 100) String pizzaName,
        @NotBlank @Pattern(regexp = "^(PEQUENA|MEDIA|GRANDE|FAMILIA)$", message = "Use PEQUENA, MEDIA, GRANDE ou FAMILIA") String size,
        @NotNull @Min(1) @Max(20) Integer quantity,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 8, fraction = 2) BigDecimal unitPrice
    ) {}
}
