package by.ilyatr.currencyclientstarter.service;

import by.ilyatr.currencyclientstarter.dto.ExchangeRateResponse;
import by.ilyatr.currencyclientstarter.properties.CurrencyClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;

@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

  private final RestTemplate restTemplate;
  private final CurrencyClientProperties properties;

  @Override
  public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
    String url = properties.getBaseUrl()
                 + "/v2/rate/"
                 + fromCurrency.toLowerCase()
                 + "/"
                 + toCurrency.toLowerCase();

    ExchangeRateResponse response = restTemplate.getForObject(url, ExchangeRateResponse.class);
    if (response == null) {
      throw new IllegalStateException(
          "Empty response from currency API"
      );
    }
    return response.rate();
  }
}
