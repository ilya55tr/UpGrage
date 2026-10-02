package by.ilyatr.currencyclientstarter.dto;

import java.math.BigDecimal;

public record ExchangeRateResponse(
    BigDecimal amount,
    String base,
    String date,
    BigDecimal rate
) {
}
