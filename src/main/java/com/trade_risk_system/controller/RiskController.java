package com.trade_risk_system.controller;

import com.trade_risk_system.dto.common.ApiResponse;
import com.trade_risk_system.dto.response.RiskResponse;
import com.trade_risk_system.service.RiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
public class RiskController {

    private final RiskService riskService;

    @GetMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<RiskResponse>> getRisk(@PathVariable Long portfolioId) {
        return ResponseEntity.ok(ApiResponse.success(riskService.calculatePortfolioRisk(portfolioId)));
    }

    @GetMapping("/{portfolioId}/history")
    public ResponseEntity<ApiResponse<?>> getRiskHistory(
            @PathVariable Long portfolioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @PageableDefault(size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        if (start != null && end != null) {
            return ResponseEntity
                    .ok(ApiResponse.fromPage(riskService.getRiskHistoryByDate(portfolioId, start, end, pageable)));
        }
        return ResponseEntity.ok(ApiResponse.fromPage(riskService.getRiskHistory(portfolioId, pageable)));
    }
}
