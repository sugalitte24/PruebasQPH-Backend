package com.example.demo.service;

import com.example.demo.dto.SaleItemRequest;
import com.example.demo.dto.SaleItemResponse;
import com.example.demo.dto.SaleRequest;
import com.example.demo.dto.SaleResponse;
import com.example.demo.entity.Product;
import com.example.demo.entity.Sale;
import com.example.demo.entity.SaleItem;
import com.example.demo.entity.User;
import com.example.demo.exception.InsufficientStockException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.SaleRepository;
import com.example.demo.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public SaleResponse create(SaleRequest request, String username) {
        List<SaleItemRequest> items = request.items().stream()
                .sorted(Comparator.comparing(SaleItemRequest::productId))
                .toList();

        for (SaleItemRequest item : items) {
            int updated = productRepository.decrementStock(item.productId(), item.quantity());
            if (updated == 0) {
                Product product = productRepository.findById(item.productId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Producto no encontrado con id: " + item.productId()));
                throw new InsufficientStockException(String.format(
                        "Stock insuficiente para '%s'. Disponible: %d, solicitado: %d",
                        product.getNombre(), product.getStock(), item.quantity()));
            }
        }

        // decrementStock limpia el contexto de persistencia: las entidades se leen después
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        Sale sale = Sale.builder()
                .invoiceNumber(UUID.randomUUID().toString())
                .user(user)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (SaleItemRequest item : items) {
            Product product = productRepository.findById(item.productId()).orElseThrow();
            BigDecimal subtotal = product.getPrecio().multiply(BigDecimal.valueOf(item.quantity()));

            sale.addItem(SaleItem.builder()
                    .product(product)
                    .quantity(item.quantity())
                    .unitPrice(product.getPrecio())
                    .subtotal(subtotal)
                    .build());
            total = total.add(subtotal);
        }
        sale.setTotal(total);

        return toResponse(saleRepository.save(sale));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> findAll(String username, boolean isAdmin) {
        List<Sale> sales = isAdmin
                ? saleRepository.findAllByOrderByCreatedAtDesc()
                : saleRepository.findByUserUsernameOrderByCreatedAtDesc(username);
        return sales.stream().map(this::toResponse).toList();
    }

    private SaleResponse toResponse(Sale s) {
        List<SaleItemResponse> items = s.getItems().stream()
                .map(i -> new SaleItemResponse(
                        i.getProduct().getId(),
                        i.getProduct().getNombre(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getSubtotal()))
                .toList();
        return new SaleResponse(s.getId(), s.getInvoiceNumber(), s.getCreatedAt(),
                s.getUser().getUsername(), s.getTotal(), items);
    }
}
