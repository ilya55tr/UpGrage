package by.ilyatr.msaccountreservation.config;

import java.math.BigDecimal;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class CacheConfig {

  @Bean
  public RedisCacheManagerBuilderCustomizer exchangeRatesCacheCustomizer(
      @Value("${spring.cache.redis.time-to-live}") Duration ttl
  ){
    var serializer = new Jackson2JsonRedisSerializer<>(BigDecimal.class);
    var configuration = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(ttl)
        .disableCachingNullValues()
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

    return builder -> builder.withCacheConfiguration(
        "exchangeRates", configuration
    );

  }
}
