package com.trade_risk_system.service;

import com.trade_risk_system.dto.request.InstrumentRequest;
import com.trade_risk_system.dto.response.InstrumentResponse;
import com.trade_risk_system.exception.DuplicateResourceException;
import com.trade_risk_system.exception.ResourceNotFoundException;
import com.trade_risk_system.model.Instrument;
import com.trade_risk_system.repository.InstrumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InstrumentService {

    private static final Logger log = LoggerFactory.getLogger(InstrumentService.class);

    private final InstrumentRepository instrumentRepository;
    private final CacheEvictionService cacheEvictionService;

    public InstrumentService(InstrumentRepository instrumentRepository,
            CacheEvictionService cacheEvictionService) {
        this.instrumentRepository = instrumentRepository;
        this.cacheEvictionService = cacheEvictionService;
    }

    @Transactional
    public InstrumentResponse createInstrument(InstrumentRequest request) {
        log.info("Creating instrument: {}", request.symbol());
        if (instrumentRepository.existsBySymbol(request.symbol())) {
            throw new DuplicateResourceException("Instrument with symbol " + request.symbol() + " already exists");
        }

        Instrument instrument = Instrument.builder()
                .symbol(request.symbol())
                .name(request.name())
                .currentPrice(request.currentPrice())
                .build();

        Instrument savedInstrument = instrumentRepository.save(instrument);
        log.info("Instrument created: {}", savedInstrument.getId());
        cacheEvictionService.evictInstrumentCaches();
        return mapToResponse(savedInstrument);
    }

    @Cacheable(value = "instruments-list", keyGenerator = "pageableCacheKeyGenerator")
    public Page<InstrumentResponse> getAllInstruments(Pageable pageable) {
        log.debug("Fetching all instruments, page: {}", pageable.getPageNumber());
        return instrumentRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Cacheable(value = "instruments", key = "#id")
    public InstrumentResponse getInstrumentById(Long id) {
        Instrument instrument = instrumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instrument not found with id: " + id));
        return mapToResponse(instrument);
    }

    private InstrumentResponse mapToResponse(Instrument instrument) {
        return new InstrumentResponse(
                instrument.getId(),
                instrument.getSymbol(),
                instrument.getName(),
                instrument.getCurrentPrice());
    }
}
