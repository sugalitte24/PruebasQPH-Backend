package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SaleRequest(
        @NotEmpty(message = "La venta debe tener al menos un producto")
        List<@Valid SaleItemRequest> items
) {}
