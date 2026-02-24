package com.trade_risk_system.service;

import com.trade_risk_system.dto.request.TradeRequest;
import com.trade_risk_system.dto.response.TradeResponse;
import com.trade_risk_system.exception.ResourceNotFoundException;
import com.trade_risk_system.exception.TradeValidationException;
import com.trade_risk_system.model.*;
import com.trade_risk_system.model.enums.TradeSide;
import com.trade_risk_system.model.enums.TradeStatus;
import com.trade_risk_system.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Core business service for managing trade lifecycles and portfolio holdings.
 * Implements high-performance trade execution with immediate risk impact.
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class TradeService {

    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;
    private final InstrumentRepository instrumentRepository;
    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final AuditService auditService;
    private final CacheEvictionService cacheEvictionService;

    public TradeService(TradeRepository tradeRepository,
            UserRepository userRepository,
            InstrumentRepository instrumentRepository,
            PortfolioRepository portfolioRepository,
            PositionRepository positionRepository,
            AuditService auditService,
            CacheEvictionService cacheEvictionService) {
        this.tradeRepository = tradeRepository;
        this.userRepository = userRepository;
        this.instrumentRepository = instrumentRepository;
        this.portfolioRepository = portfolioRepository;
        this.positionRepository = positionRepository;
        this.auditService = auditService;
        this.cacheEvictionService = cacheEvictionService;
    }

    /**
     * Executes a trade within a transactional boundary.
     * Transaction Flow: PENDING -> Position Update -> Portfolio Recalculation ->
     * EXECUTED.
     * 
     * @param request The trade request details
     * @param userId  The ID of the user executing the trade
     * @return Standardized TradeResponse
     * @throws ResourceNotFoundException if user or instrument is missing
     * @throws TradeValidationException  if sell quantity exceeds held position
     */
    @Transactional
    public TradeResponse executeTrade(TradeRequest request, Long userId) {
        log.info("TRADE_EXECUTION_START | User: {} | Instrument: {} | Side: {} | Qty: {}",
                userId, request.instrumentId(), request.side(), request.quantity());

        // A) Validate Instrument
        Instrument instrument = instrumentRepository.findById(request.instrumentId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Instrument not found with id: " + request.instrumentId()));

        // B) Fetch User and Portfolio
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found for user: " + userId));

        // C) Validation for SELL
        if (request.side() == TradeSide.SELL) {
            Position position = positionRepository
                    .findByPortfolioIdAndInstrumentId(portfolio.getId(), instrument.getId())
                    .orElseThrow(() -> new TradeValidationException(
                            "Permission denied: No position held in " + instrument.getSymbol()));

            if (request.quantity() > position.getQuantity()) {
                throw new TradeValidationException("Insufficient quantity. Available: " + position.getQuantity());
            }
        }

        // D) Create PENDING Trade
        Trade trade = Trade.builder()
                .user(user)
                .instrument(instrument)
                .quantity(request.quantity())
                .price(request.price())
                .side(request.side())
                .status(TradeStatus.PENDING)
                .timestamp(LocalDateTime.now())
                .build();

        // E) Persist Trade (to get ID)
        trade = tradeRepository.save(trade);

        // F) Update or create Position
        updatePosition(portfolio, instrument, request.quantity(), request.price(), request.side());

        // G) Update Portfolio totalValue (Recalculate from all positions)
        recalculatePortfolioValue(portfolio);

        // H) Mark EXECUTED
        trade.setStatus(TradeStatus.EXECUTED);
        tradeRepository.save(trade);

        // I) Audit Integration
        auditService.logAction("TRADE_EXECUTED", userId);

        // J) Evict caches for the affected portfolio only
        Long portfolioId = portfolio.getId();
        cacheEvictionService.evictPortfolioCache(portfolioId);
        cacheEvictionService.evictRiskCache(portfolioId);

        log.info("TRADE_EXECUTION_SUCCESS | TradeID: {} | User: {} | Status: EXECUTED", trade.getId(), userId);
        return mapToResponse(trade);
    }

    private void updatePosition(Portfolio portfolio, Instrument instrument, Integer quantity,
            java.math.BigDecimal price,
            TradeSide side) {
        Optional<Position> positionOptional = positionRepository.findByPortfolioIdAndInstrumentId(portfolio.getId(),
                instrument.getId());

        if (side == TradeSide.BUY) {
            if (positionOptional.isPresent()) {
                Position position = positionOptional.get();
                // Formula: newAvgPrice = ((oldQty * oldAvg) + (newQty * tradePrice)) / (oldQty
                // + newQty)
                java.math.BigDecimal oldTotalCost = position.getAvgPrice()
                        .multiply(java.math.BigDecimal.valueOf(position.getQuantity()));
                java.math.BigDecimal newTradeCost = price
                        .multiply(java.math.BigDecimal.valueOf(quantity));
                int newTotalQty = position.getQuantity() + quantity;

                position.setQuantity(newTotalQty);
                position.setAvgPrice(oldTotalCost.add(newTradeCost)
                        .divide(java.math.BigDecimal.valueOf(newTotalQty),
                                com.trade_risk_system.util.MoneyUtils.PRICE_SCALE,
                                com.trade_risk_system.util.MoneyUtils.ROUNDING));
                positionRepository.save(position);
            } else {
                Position newPosition = Position.builder()
                        .portfolio(portfolio)
                        .instrument(instrument)
                        .quantity(quantity)
                        .avgPrice(price)
                        .build();
                positionRepository.save(newPosition);
            }
        } else { // SELL
            Position position = positionOptional.get(); // Already validated in executeTrade
            int newQty = position.getQuantity() - quantity;
            if (newQty == 0) {
                positionRepository.delete(position);
            } else {
                position.setQuantity(newQty);
                positionRepository.save(position);
            }
        }
    }

    private void recalculatePortfolioValue(Portfolio portfolio) {
        List<Position> positions = positionRepository.findByPortfolioIdWithInstrument(portfolio.getId());
        java.math.BigDecimal totalValue = positions.stream()
                .map(p -> {
                    java.math.BigDecimal currentPrice = p.getInstrument().getCurrentPrice();
                    java.math.BigDecimal priceToUse = (currentPrice != null) ? currentPrice : p.getAvgPrice();
                    return priceToUse.multiply(java.math.BigDecimal.valueOf(p.getQuantity()));
                })
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        portfolio.setTotalValue(com.trade_risk_system.util.MoneyUtils.scale(totalValue));
        portfolioRepository.save(portfolio);
    }

    public Page<TradeResponse> getAllTrades(Pageable pageable) {
        return tradeRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public Page<TradeResponse> getTradesByInstrument(Long instrumentId, Pageable pageable) {
        return tradeRepository.findByInstrumentId(instrumentId, pageable)
                .map(this::mapToResponse);
    }

    public Page<TradeResponse> getTradesByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return tradeRepository.findByTimestampBetween(start, end, pageable)
                .map(this::mapToResponse);
    }

    private TradeResponse mapToResponse(Trade trade) {
        return new TradeResponse(
                trade.getId(),
                trade.getUser().getId(),
                trade.getInstrument().getId(),
                trade.getInstrument().getSymbol(),
                trade.getQuantity(),
                trade.getPrice(),
                trade.getSide(),
                trade.getStatus(),
                trade.getTimestamp());
    }
}
