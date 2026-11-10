package by.ilyatr.currencyclientstarter.service;

import by.ilyatr.currencyclientstarter.client.CurrencyFeignClient;
import by.ilyatr.currencyclientstarter.dto.ExchangeRateResponse;
import by.ilyatr.currencyclientstarter.exception.CurrencyApiInvalidResponseException;
import by.ilyatr.currencyclientstarter.exception.CurrencyApiRateLimitExceededException;
import by.ilyatr.currencyclientstarter.exception.CurrencyApiUnavailableException;
import feign.RetryableException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.Counter;
import java.math.BigDecimal;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;


@RequiredArgsConstructor
public class FeignCurrencyService implements CurrencyService{

  private final CurrencyFeignClient client;
  private final Retry retry;
  private final RateLimiter rateLimiter;
  private final Counter exchangeRateRequests;

  @Override
  @Cacheable(
      cacheNames = "exchangeRates",
      key = "#p0.toUpperCase(T(java.util.Locale).ROOT)+':'" +
            "+#p1.toUpperCase(T(java.util.Locale).ROOT)",
      condition = "!#p0.equalsIgnoreCase(#p1)"
  )
  public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
    if(fromCurrency.equalsIgnoreCase(toCurrency)){
      return BigDecimal.ONE;
    }

    Supplier<ExchangeRateResponse> call = () -> {
      exchangeRateRequests.increment();
      return client.getRate(fromCurrency.toLowerCase(), toCurrency.toLowerCase());
    };
    Supplier<ExchangeRateResponse> decorated = Retry.decorateSupplier(retry, RateLimiter.decorateSupplier(rateLimiter, call));

    ExchangeRateResponse response;
    try {
      response = decorated.get();
    }catch(RequestNotPermitted e){
      throw new CurrencyApiRateLimitExceededException(e);
    }catch (RetryableException e){
      throw new CurrencyApiUnavailableException(e);
    }
    if (response == null || response.rate() == null){
      throw new CurrencyApiInvalidResponseException("Currency API returned empty rate for " + fromCurrency + " to " + toCurrency);
    }
    return response.rate();
  }

}
