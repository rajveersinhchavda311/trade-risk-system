package com.trade_risk_system.controller;

import com.trade_risk_system.dto.common.ApiResponse;
import com.trade_risk_system.dto.request.PortfolioRequestDTO;
import com.trade_risk_system.dto.response.PortfolioResponse;
import com.trade_risk_system.service.PortfolioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PortfolioResponse>> createPortfolio(
            @Valid @RequestBody PortfolioRequestDTO request) {
        PortfolioResponse response = portfolioService.createPortfolio(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Portfolio created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PortfolioResponse>> getPortfolioById(@PathVariable Long id) {
        PortfolioResponse response = portfolioService.getPortfolioById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllPortfolios(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.fromPage(portfolioService.getAllPortfolios(pageable)));
    }
}
