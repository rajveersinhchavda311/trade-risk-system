package com.trade_risk_system.repository;

import com.trade_risk_system.model.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

        @Query("SELECT t FROM Trade t WHERE t.user.id = (SELECT p.user.id FROM Portfolio p WHERE p.id = :portfolioId)")
        Page<Trade> findByPortfolioId(@Param("portfolioId") Long portfolioId, Pageable pageable);

        Page<Trade> findByInstrumentId(Long instrumentId, Pageable pageable);

        Page<Trade> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

        @Query("SELECT t FROM Trade t WHERE t.instrument.id = :instrumentId AND t.timestamp BETWEEN :start AND :end")
        Page<Trade> findByInstrumentIdAndTimestampBetween(
                        @Param("instrumentId") Long instrumentId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        Pageable pageable);
}
