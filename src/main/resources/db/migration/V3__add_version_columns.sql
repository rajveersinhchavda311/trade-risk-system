-- V3: Add version column for optimistic locking on positions
ALTER TABLE positions ADD COLUMN version BIGINT DEFAULT 0;
