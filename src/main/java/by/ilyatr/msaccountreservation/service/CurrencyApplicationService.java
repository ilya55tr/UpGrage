package by.ilyatr.msaccountreservation.service;

import by.ilyatr.currencyclientstarter.service.CurrencyService;
import by.ilyatr.msaccountreservation.exception.CurrencyClientException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyApplicationService {

  private final CurrencyService currencyService;

  public BigDecimal getExchangeRate(String from, String to) {
    try {
      return currencyService.getExchangeRate(from, to);
    } catch (HttpStatusCodeException e) {
      log.error("Currency API returned HTTP {} for {} to {}", e.getStatusCode(), from, to, e);
      throw new CurrencyClientException("Currency API returned HTTP " + e.getStatusCode(), e);
    } catch (ResourceAccessException e) {
      log.error("Currency API is unavailable for {} to {}", from, to, e);
      throw new CurrencyClientException("Currency API is unavailable", e);
    }
  }
}
