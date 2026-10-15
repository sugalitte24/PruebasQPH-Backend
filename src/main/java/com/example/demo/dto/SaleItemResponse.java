package com.example.demo.dto;

import java.math.BigDecimal;

public record SaleItemResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
