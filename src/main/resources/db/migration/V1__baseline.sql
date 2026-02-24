-- V1: Baseline schema — captures existing Hibernate-generated schema
-- This migration is applied to new databases only.
-- Existing databases are adopted via flyway baseline-on-migrate.

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'TRADER') NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS instruments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    current_price DOUBLE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS portfolios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    total_value DOUBLE,
    CONSTRAINT fk_portfolio_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_portfolio_user UNIQUE (user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS positions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    instrument_id BIGINT NOT NULL,
    quantity INT,
    avg_price DOUBLE,
    CONSTRAINT fk_position_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id),
    CONSTRAINT fk_position_instrument FOREIGN KEY (instrument_id) REFERENCES instruments(id),
    INDEX idx_position_portfolio (portfolio_id),
    INDEX idx_position_instrument (instrument_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS trades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    instrument_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL,
    side ENUM('BUY', 'SELL') NOT NULL,
    status ENUM('PENDING', 'EXECUTED', 'FAILED', 'CANCELLED') NOT NULL,
    timestamp DATETIME(6),
    CONSTRAINT fk_trade_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_trade_instrument FOREIGN KEY (instrument_id) REFERENCES instruments(id),
    INDEX idx_trade_user (user_id),
    INDEX idx_trade_instrument (instrument_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS risk_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    total_exposure DOUBLE,
    concentration_risk DOUBLE,
    risk_score DOUBLE,
    timestamp DATETIME(6),
    CONSTRAINT fk_risk_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id),
    INDEX idx_risk_portfolio (portfolio_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(255),
    user_id BIGINT,
    timestamp DATETIME(6),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;
