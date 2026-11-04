package by.ilyatr.currencyclientstarter.client;

import by.ilyatr.currencyclientstarter.dto.ExchangeRateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "currency-client",
    url = "${app.currency-client.base-url}",
    configuration = CurrencyFeignConfig.class
)
public interface CurrencyFeignClient {

  @GetMapping("${app.currency-client.rate-path}/{from}/{to}")
  ExchangeRateResponse getRate(@PathVariable("from") String from, @PathVariable("to") String to);
}
