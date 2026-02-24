package com.trade_risk_system.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(10)); // Default TTL

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                "instruments", defaultConfig.entryTtl(Duration.ofMinutes(60)),
                "instruments-list", defaultConfig.entryTtl(Duration.ofMinutes(10)),
                "portfolios", defaultConfig.entryTtl(Duration.ofMinutes(10)),
                "risk", defaultConfig.entryTtl(Duration.ofMinutes(5)),
                "userDetails", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware()
                .build();
    }

    @Bean("pageableCacheKeyGenerator")
    public KeyGenerator pageableCacheKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder(method.getName());
            for (Object param : params) {
                if (param instanceof Pageable p) {
                    sb.append(":p").append(p.getPageNumber())
                            .append(":s").append(p.getPageSize())
                            .append(":sort").append(p.getSort());
                } else {
                    sb.append(":").append(param);
                }
            }
            return sb.toString();
        };
    }

    @Override
    @Bean
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, @Nullable Object key) {
                log.warn("Cache GET failed [{}:{}]: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, @Nullable Object key,
                    @Nullable Object value) {
                log.warn("Cache PUT failed [{}:{}]: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, @Nullable Object key) {
                log.warn("Cache EVICT failed [{}:{}]: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Cache CLEAR failed [{}]: {}", cache.getName(), exception.getMessage());
            }
        };
    }
}
