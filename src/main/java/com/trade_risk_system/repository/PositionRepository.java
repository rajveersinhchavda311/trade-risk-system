package com.trade_risk_system.repository;

import com.trade_risk_system.model.Position;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByPortfolioId(Long portfolioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Position p WHERE p.portfolio.id = :portfolioId AND p.instrument.id = :instrumentId")
    Optional<Position> findByPortfolioIdAndInstrumentId(
            @Param("portfolioId") Long portfolioId,
            @Param("instrumentId") Long instrumentId);

    @Query("SELECT p FROM Position p JOIN FETCH p.instrument WHERE p.portfolio.id = :portfolioId")
    List<Position> findByPortfolioIdWithInstrument(@Param("portfolioId") Long portfolioId);
}
