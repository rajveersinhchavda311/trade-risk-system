package com.trade_risk_system.controller;

import com.trade_risk_system.dto.common.ApiResponse;
import com.trade_risk_system.dto.request.InstrumentRequest;
import com.trade_risk_system.dto.response.InstrumentResponse;
import com.trade_risk_system.service.InstrumentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllInstruments(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.fromPage(instrumentService.getAllInstruments(pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InstrumentResponse>> createInstrument(
            @Valid @RequestBody InstrumentRequest request) {
        InstrumentResponse response = instrumentService.createInstrument(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Instrument created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InstrumentResponse>> getInstrumentById(@PathVariable Long id) {
        InstrumentResponse response = instrumentService.getInstrumentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
