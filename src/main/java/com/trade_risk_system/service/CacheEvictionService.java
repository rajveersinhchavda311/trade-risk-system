package com.trade_risk_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Centralized cache eviction service.
 * All cache invalidation flows through this service to maintain a single
 * source of truth for eviction logic.
 *
 * Integration contract: Any future endpoint that modifies User, Portfolio,
 * Instrument, or Position data MUST call the appropriate eviction method.
 */
@Service
public class CacheEvictionService {

    private static final Logger log = LoggerFactory.getLogger(CacheEvictionService.class);

    private final CacheManager cacheManager;

    public CacheEvictionService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Evicts a single portfolio entry from the cache.
     * Must be called after any operation that changes a portfolio's totalValue.
     */
    public void evictPortfolioCache(Long portfolioId) {
        evictKey("portfolios", portfolioId);
    }

    /**
     * Evicts a single risk entry from the cache.
     * Must be called after any operation that changes positions within a portfolio.
     */
    public void evictRiskCache(Long portfolioId) {
        evictKey("risk", portfolioId);
    }

    /**
     * Evicts a user's cached security details.
     * Must be called after registration, role changes, or password updates.
     */
    public void evictUserDetailsCache(String username) {
        evictKey("userDetails", username);
    }

    /**
     * Evicts all instrument caches (individual + list).
     * Called when instruments are created or modified.
     */
    public void evictInstrumentCaches() {
        clearCache("instruments");
        clearCache("instruments-list");
    }

    private void evictKey(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.debug("Cache evicted [{}:{}]", cacheName, key);
        }
    }

    private void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.debug("Cache cleared [{}]", cacheName);
        }
    }
}
