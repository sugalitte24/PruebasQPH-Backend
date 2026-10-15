package com.example.demo.repository;

import com.example.demo.entity.Sale;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Sale> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Sale> findByUserUsernameOrderByCreatedAtDesc(String username);
}
