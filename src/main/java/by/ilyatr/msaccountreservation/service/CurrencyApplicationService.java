package by.ilyatr.msaccountreservation.service;

import by.ilyatr.currencyclientstarter.service.CurrencyService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyApplicationService {

  private final CurrencyService currencyService;

  public BigDecimal getExchangeRate(String from, String to) {
      return currencyService.getExchangeRate(from, to);
  }
}
