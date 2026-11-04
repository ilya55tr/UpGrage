package by.ilyatr.currencyclientstarter.service;

import java.math.BigDecimal;

public interface CurrencyService {

  BigDecimal getExchangeRate(
      String fromCurrency,
      String toCurrency
  );
}