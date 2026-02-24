package com.trade_risk_system.service;

import com.trade_risk_system.dto.request.PortfolioRequestDTO;
import com.trade_risk_system.dto.response.PortfolioResponse;
import com.trade_risk_system.exception.DuplicateResourceException;
import com.trade_risk_system.exception.ResourceNotFoundException;
import com.trade_risk_system.model.Portfolio;
import com.trade_risk_system.model.User;
import com.trade_risk_system.repository.PortfolioRepository;
import com.trade_risk_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PortfolioService {

    private static final Logger log = LoggerFactory.getLogger(PortfolioService.class);

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final CacheEvictionService cacheEvictionService;

    public PortfolioService(PortfolioRepository portfolioRepository,
            UserRepository userRepository,
            CacheEvictionService cacheEvictionService) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
        this.cacheEvictionService = cacheEvictionService;
    }

    @Transactional
    public PortfolioResponse createPortfolio(PortfolioRequestDTO request) {
        log.info("Creating portfolio for user: {}", request.userId());
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));

        if (portfolioRepository.findByUserId(request.userId()).isPresent()) {
            throw new DuplicateResourceException("User already has a portfolio");
        }

        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .totalValue(java.math.BigDecimal.ZERO)
                .build();

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        log.info("Portfolio created: {}", savedPortfolio.getId());
        cacheEvictionService.evictPortfolioCache(savedPortfolio.getId());
        return mapToResponse(savedPortfolio);
    }

    @Cacheable(value = "portfolios", key = "#id")
    public PortfolioResponse getPortfolioById(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + id));
        return mapToResponse(portfolio);
    }

    public Page<PortfolioResponse> getAllPortfolios(Pageable pageable) {
        log.debug("Fetching all portfolios, page: {}", pageable.getPageNumber());
        return portfolioRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    private PortfolioResponse mapToResponse(Portfolio portfolio) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getUser().getId(),
                portfolio.getUser().getUsername(),
                portfolio.getTotalValue());
    }
}
