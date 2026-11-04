package by.ilyatr.currencyclientstarter.autoconfigure;

import by.ilyatr.currencyclientstarter.client.CurrencyFeignClient;
import by.ilyatr.currencyclientstarter.exception.CurrencyApiServerErrorException;
import by.ilyatr.currencyclientstarter.health.CurrencyApiHealthIndicator;
import by.ilyatr.currencyclientstarter.properties.CurrencyClientProperties;
import by.ilyatr.currencyclientstarter.service.CurrencyService;
import by.ilyatr.currencyclientstarter.service.FeignCurrencyService;
import feign.RetryableException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
@EnableConfigurationProperties(CurrencyClientProperties.class)
@ConditionalOnExpression("${app.currency-client.enabled:false}")
@EnableFeignClients(clients = CurrencyFeignClient.class)
public class CurrencyClientAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(name = "currencyApiRetry")
  public Retry currencyApiRetry(CurrencyClientProperties properties) {
    var retry = properties.getRetry();
    RetryConfig config = RetryConfig.custom()
        .maxAttempts(retry.getMaxAttempts())
        .waitDuration(retry.getWaitDuration())
        .retryExceptions(RetryableException.class, CurrencyApiServerErrorException.class)
        .build();
    return Retry.of("currencyApi", config);
  }

  @Bean
  @ConditionalOnMissingBean(name = "currencyApiRateLimiter")
  public RateLimiter currencyApiRateLimiter(CurrencyClientProperties properties) {
    var rl = properties.getRateLimiter();
    RateLimiterConfig config = RateLimiterConfig.custom()
        .limitForPeriod(rl.getLimitForPeriod())
        .limitRefreshPeriod(rl.getLimitRefreshPeriod())
        .timeoutDuration(rl.getTimeoutDuration())
        .build();
    return RateLimiter.of("currencyApi", config);
  }

  @Bean
  @ConditionalOnMissingBean(CurrencyService.class)
  public CurrencyService currencyService(CurrencyFeignClient client, Retry currencyApiRetry, RateLimiter currencyApiRateLimiter) {
    return new FeignCurrencyService(client, currencyApiRetry, currencyApiRateLimiter);
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(HealthIndicator.class)
  @ConditionalOnExpression("${app.currency-client.health.enabled:true}")
  static class HealthConfiguration {

    @Bean
    public CurrencyApiHealthIndicator currencyApiHealthIndicator(CurrencyFeignClient client, CurrencyClientProperties properties) {
      return new CurrencyApiHealthIndicator(client, properties);
    }
  }
}
