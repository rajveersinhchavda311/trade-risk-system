package com.trade_risk_system.controller;

import com.trade_risk_system.dto.common.ApiResponse;
import com.trade_risk_system.dto.request.TradeRequest;
import com.trade_risk_system.dto.response.TradeResponse;
import com.trade_risk_system.service.TradeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TradeResponse>> executeTrade(@Valid @RequestBody TradeRequest request) {
        TradeResponse response = tradeService.executeTrade(request, request.userId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trade executed successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllTrades(
            @RequestParam(required = false) Long instrumentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        if (instrumentId != null) {
            return ResponseEntity.ok(ApiResponse.fromPage(tradeService.getTradesByInstrument(instrumentId, pageable)));
        }
        if (start != null && end != null) {
            return ResponseEntity.ok(ApiResponse.fromPage(tradeService.getTradesByDateRange(start, end, pageable)));
        }
        return ResponseEntity.ok(ApiResponse.fromPage(tradeService.getAllTrades(pageable)));
    }
}
