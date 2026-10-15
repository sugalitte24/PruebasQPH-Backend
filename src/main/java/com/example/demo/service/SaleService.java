package com.example.demo.service;

import com.example.demo.dto.SaleRequest;
import com.example.demo.dto.SaleResponse;
import java.util.List;

public interface SaleService {

    SaleResponse create(SaleRequest request, String username);

    List<SaleResponse> findAll(String username, boolean isAdmin);
}
