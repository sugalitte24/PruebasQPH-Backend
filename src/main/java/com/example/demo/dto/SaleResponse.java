package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponse(
        Long id,
        String invoiceNumber,
        LocalDateTime createdAt,
        String username,
        BigDecimal total,
        List<SaleItemResponse> items
) {}
