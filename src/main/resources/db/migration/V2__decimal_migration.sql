-- V2: Migrate financial columns from DOUBLE to DECIMAL for precision
-- DECIMAL(19,4) for monetary values, DECIMAL(19,8) for ratios

ALTER TABLE instruments    MODIFY COLUMN current_price DECIMAL(19,4);
ALTER TABLE trades         MODIFY COLUMN price DECIMAL(19,4) NOT NULL;
ALTER TABLE positions      MODIFY COLUMN avg_price DECIMAL(19,4);
ALTER TABLE portfolios     MODIFY COLUMN total_value DECIMAL(19,4);
ALTER TABLE risk_metrics   MODIFY COLUMN total_exposure DECIMAL(19,4);
ALTER TABLE risk_metrics   MODIFY COLUMN concentration_risk DECIMAL(19,8);
ALTER TABLE risk_metrics   MODIFY COLUMN risk_score DECIMAL(19,4);
