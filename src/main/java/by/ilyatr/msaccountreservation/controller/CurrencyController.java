package by.ilyatr.msaccountreservation.controller;

import by.ilyatr.msaccountreservation.api.CurrenciesApi;
import by.ilyatr.msaccountreservation.model.ExchangeRateDto;
import by.ilyatr.msaccountreservation.service.CurrencyApplicationService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CurrencyController implements CurrenciesApi {

  private final CurrencyApplicationService currencyApplicationService;

  @Override
  public ResponseEntity<ExchangeRateDto> getExchangeRate(String from, String to) {
    BigDecimal rate = currencyApplicationService.getExchangeRate(from, to);
    return ResponseEntity.ok(
        ExchangeRateDto.builder()
            .from(from.toUpperCase())
            .to(to.toUpperCase())
            .rate(rate)
            .build()
    );
  }
}
