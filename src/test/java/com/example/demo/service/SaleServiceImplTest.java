package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dto.SaleItemRequest;
import com.example.demo.dto.SaleRequest;
import com.example.demo.dto.SaleResponse;
import com.example.demo.entity.Product;
import com.example.demo.entity.Role;
import com.example.demo.entity.Sale;
import com.example.demo.entity.User;
import com.example.demo.exception.InsufficientStockException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.SaleRepository;
import com.example.demo.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SaleServiceImpl saleService;

    private User seller;
    private Product laptop;
    private Product mouse;

    @BeforeEach
    void setUp() {
        seller = User.builder().id(1L).username("vendedor").role(Role.USER).build();
        laptop = Product.builder().id(1L).nombre("Laptop").precio(new BigDecimal("1000.00")).stock(5).build();
        mouse = Product.builder().id(2L).nombre("Mouse").precio(new BigDecimal("25.50")).stock(10).build();
    }

    @Test
    @DisplayName("Registra la venta, descuenta stock y calcula subtotales y total")
    void create_success() {
        when(productRepository.decrementStock(anyLong(), anyInt())).thenReturn(1);
        when(userRepository.findByUsername("vendedor")).thenReturn(Optional.of(seller));
        when(productRepository.findById(1L)).thenReturn(Optional.of(laptop));
        when(productRepository.findById(2L)).thenReturn(Optional.of(mouse));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        SaleResponse response = saleService.create(request(item(1L, 2), item(2L, 3)), "vendedor");

        assertThat(response.username()).isEqualTo("vendedor");
        assertThat(response.invoiceNumber()).isNotBlank();
        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).subtotal()).isEqualByComparingTo("2000.00");
        assertThat(response.items().get(1).subtotal()).isEqualByComparingTo("76.50");
        assertThat(response.total()).isEqualByComparingTo("2076.50");
        verify(productRepository).decrementStock(1L, 2);
        verify(productRepository).decrementStock(2L, 3);
    }

    @Test
    @DisplayName("Descuenta el stock en orden de productId para evitar deadlocks")
    void create_decrementsInProductIdOrder() {
        when(productRepository.decrementStock(anyLong(), anyInt())).thenReturn(1);
        when(userRepository.findByUsername("vendedor")).thenReturn(Optional.of(seller));
        when(productRepository.findById(1L)).thenReturn(Optional.of(laptop));
        when(productRepository.findById(2L)).thenReturn(Optional.of(mouse));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        saleService.create(request(item(2L, 1), item(1L, 1)), "vendedor");

        InOrder order = inOrder(productRepository);
        order.verify(productRepository).decrementStock(1L, 1);
        order.verify(productRepository).decrementStock(2L, 1);
    }

    @Test
    @DisplayName("Lanza InsufficientStockException y no guarda la venta si no hay stock")
    void create_insufficientStock() {
        when(productRepository.decrementStock(1L, 99)).thenReturn(0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(laptop));

        assertThatThrownBy(() -> saleService.create(request(item(1L, 99)), "vendedor"))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Laptop")
                .hasMessageContaining("Disponible: 5");

        verify(saleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lanza ResourceNotFoundException si el producto no existe")
    void create_productNotFound() {
        when(productRepository.decrementStock(99L, 1)).thenReturn(0);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.create(request(item(99L, 1)), "vendedor"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(saleRepository, never()).save(any());
    }

    @Test
    @DisplayName("ADMIN consulta todas las ventas; USER solo las propias")
    void findAll_filtersByRole() {
        when(saleRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(saleRepository.findByUserUsernameOrderByCreatedAtDesc("vendedor")).thenReturn(List.of());

        saleService.findAll("admin", true);
        saleService.findAll("vendedor", false);

        verify(saleRepository).findAllByOrderByCreatedAtDesc();
        verify(saleRepository).findByUserUsernameOrderByCreatedAtDesc("vendedor");
    }

    private static SaleItemRequest item(Long productId, int quantity) {
        return new SaleItemRequest(productId, quantity);
    }

    private static SaleRequest request(SaleItemRequest... items) {
        return new SaleRequest(List.of(items));
    }
}
