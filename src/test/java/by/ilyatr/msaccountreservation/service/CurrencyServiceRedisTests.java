package by.ilyatr.msaccountreservation.service;

import by.ilyatr.currencyclientstarter.autoconfigure.CurrencyClientAutoConfiguration;
import by.ilyatr.currencyclientstarter.client.CurrencyFeignClient;
import by.ilyatr.currencyclientstarter.dto.ExchangeRateResponse;
import by.ilyatr.currencyclientstarter.exception.CurrencyApiInvalidResponseException;
import by.ilyatr.currencyclientstarter.exception.CurrencyApiServerErrorException;
import by.ilyatr.currencyclientstarter.service.CurrencyService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


@DataRedisTest(properties = {
    "app.currency-client.enabled=true",
    "app.currency-client.base-url=http://localhost",
    "app.currency-client.rate-path=/v2/rate",
    "app.currency-client.rate-limiter.limit-for-period=1000",
    "app.currency-client.retry.wait-duration=1ms"
})
@AutoConfigureCache(cacheProvider = CacheType.REDIS)
@ImportAutoConfiguration({
    CurrencyClientAutoConfiguration.class,
    FeignAutoConfiguration.class,
    MetricsAutoConfiguration.class,
    SimpleMetricsExportAutoConfiguration.class
})
@Testcontainers
public class CurrencyServiceRedisTests {

  private static final String CACHE_NAME = "exchangeRates";
  private static final String USD_EUR_KEY = "exchangeRates::USD:EUR";

  @Container
  static final GenericContainer<?> REDIS = new GenericContainer<>(
      DockerImageName.parse("redis:7.4-alpine")
  ).withExposedPorts(6379);

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry){
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
  }

  @Autowired
  private CurrencyService currencyService;

  @Autowired
  private CacheManager cacheManager;

  @Autowired
  private StringRedisTemplate redisTemplate;

  @Autowired
  private MeterRegistry meterRegistry;

  @MockitoBean(enforceOverride = true)
  private CurrencyFeignClient client;

  private Counter requests;
  private double requestsBefore;

  @BeforeEach
  void setUp(){
    var cache = cacheManager.getCache(CACHE_NAME);
    assertThat(cache).isNotNull();
    cache.clear();

    requests = meterRegistry
        .get("currency.exchange.rate.requests")
        .counter();

    requestsBefore = requests.count();
  }

  @Test
  void repeatRequestUsesCachedRateRegardlessOfLetterCase(){
    when(client.getRate("usd", "eur")).thenReturn(response("USD", "0.92"));

    BigDecimal first = currencyService.getExchangeRate("USD", "EUR");
    BigDecimal second = currencyService.getExchangeRate("usd", "eur");

    assertThat(first).isEqualByComparingTo("0.92");
    assertThat(second).isEqualByComparingTo("0.92");
    assertThat(redisTemplate.hasKey(USD_EUR_KEY)).isTrue();

    verify(client, times(1)).getRate("usd", "eur");
    assertThat(requests.count() - requestsBefore).isEqualTo(1.0);

  }

  @Test
  void reversePairUsesSeparateCacheEntry(){
    when(client.getRate("usd", "eur"))
        .thenReturn(response("USD", "0.92"));
    when(client.getRate("eur", "usd"))
        .thenReturn(response("EUR", "1.08"));

    assertThat(currencyService.getExchangeRate("USD", "EUR"))
        .isEqualByComparingTo("0.92");
    assertThat(currencyService.getExchangeRate("EUR", "USD"))
        .isEqualByComparingTo("1.08");

    assertThat(currencyService.getExchangeRate("USD", "EUR"))
        .isEqualByComparingTo("0.92");
    assertThat(currencyService.getExchangeRate("EUR", "USD"))
        .isEqualByComparingTo("1.08");

    verify(client, times(1)).getRate("usd", "eur");
    verify(client, times(1)).getRate("eur", "usd");
    assertThat(requests.count() - requestsBefore).isEqualTo(2.0);
  }

  @Test
  void cacheEntryUsesThirtyMinuteTtlFromApplicationConfiguration() {
    when(client.getRate("usd", "eur"))
        .thenReturn(response("USD", "0.92"));

    currencyService.getExchangeRate("USD", "EUR");

    Long ttl = redisTemplate.getExpire(
        USD_EUR_KEY,
        TimeUnit.SECONDS
    );

    assertThat(ttl).isBetween(1700L, 1800L);
  }

  @Test
  void expiredRateIsLoadedAgainAndCached() {
    when(client.getRate("usd", "eur"))
        .thenReturn(
            response("USD", "0.92"),
            response("USD", "0.95")
        );

    assertThat(currencyService.getExchangeRate("USD", "EUR"))
        .isEqualByComparingTo("0.92");

    // Ускоряем истечение записи, чтобы тест не ждал 30 минут.
    Boolean expirationUpdated = redisTemplate.expire(
        USD_EUR_KEY,
        Duration.ofMillis(200)
    );
    assertThat(expirationUpdated).isTrue();

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() ->
            assertThat(redisTemplate.hasKey(USD_EUR_KEY)).isFalse()
        );

    assertThat(currencyService.getExchangeRate("USD", "EUR"))
        .isEqualByComparingTo("0.95");
    assertThat(currencyService.getExchangeRate("USD", "EUR"))
        .isEqualByComparingTo("0.95");

    verify(client, times(2)).getRate("usd", "eur");
    assertThat(requests.count() - requestsBefore).isEqualTo(2.0);
  }

  @Test
  void invalidResponseIsNotCached() {
    when(client.getRate("usd", "eur"))
        .thenReturn(
            new ExchangeRateResponse(
                BigDecimal.ONE, "USD", "2026-11-10", null
            ),
            response("USD", "0.92")
        );

    assertThatThrownBy(() ->
        currencyService.getExchangeRate("USD", "EUR")
    ).isInstanceOf(CurrencyApiInvalidResponseException.class);

    assertThat(redisTemplate.hasKey(USD_EUR_KEY)).isFalse();

    assertThat(currencyService.getExchangeRate("USD", "EUR"))
        .isEqualByComparingTo("0.92");
    assertThat(currencyService.getExchangeRate("USD", "EUR"))
        .isEqualByComparingTo("0.92");

    verify(client, times(2)).getRate("usd", "eur");
    assertThat(requests.count() - requestsBefore).isEqualTo(2.0);
  }

  @Test
  void retryCountsEachAttemptAndCachesSuccessfulResult() {
    when(client.getRate("usd", "eur"))
        .thenThrow(new CurrencyApiServerErrorException(503, "Unavailable"))
        .thenReturn(response("USD", "0.92"));

    assertThat(currencyService.getExchangeRate("USD", "EUR"))
        .isEqualByComparingTo("0.92");
    assertThat(currencyService.getExchangeRate("USD", "EUR"))
        .isEqualByComparingTo("0.92");

    verify(client, times(2)).getRate("usd", "eur");
    assertThat(requests.count() - requestsBefore).isEqualTo(2.0);
  }

  @Test
  void sameCurrencyReturnsOneWithoutExternalRequest() {
    assertThat(currencyService.getExchangeRate("USD", "usd"))
        .isEqualByComparingTo(BigDecimal.ONE);

    verifyNoInteractions(client);
    assertThat(redisTemplate.hasKey("exchangeRates::USD:USD")).isFalse();
    assertThat(requests.count()).isEqualTo(requestsBefore);
  }

  private static ExchangeRateResponse response(String base, String rate) {
    return new ExchangeRateResponse(
        BigDecimal.ONE,
        base,
        "2026-11-10",
        new BigDecimal(rate)
    );
  }

}
