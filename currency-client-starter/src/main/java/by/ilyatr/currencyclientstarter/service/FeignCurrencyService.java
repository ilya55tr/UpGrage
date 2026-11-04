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
import java.math.BigDecimal;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FeignCurrencyService implements CurrencyService{

  private final CurrencyFeignClient client;
  private final Retry retry;
  private final RateLimiter rateLimiter;

  @Override
  public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
    if(fromCurrency.equalsIgnoreCase(toCurrency)){
      return BigDecimal.ONE;
    }

    Supplier<ExchangeRateResponse> call = () -> client.getRate(fromCurrency.toLowerCase(), toCurrency.toLowerCase());
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
