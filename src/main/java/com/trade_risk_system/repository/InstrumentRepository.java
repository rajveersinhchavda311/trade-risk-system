package com.trade_risk_system.repository;

import com.trade_risk_system.model.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {
    Optional<Instrument> findBySymbol(String symbol);

    boolean existsBySymbol(String symbol);
}
