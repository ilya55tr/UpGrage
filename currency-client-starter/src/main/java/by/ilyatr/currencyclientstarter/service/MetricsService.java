package by.ilyatr.currencyclientstarter.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

public class MetricsService {

  private final Counter exchangeRateRequests;

  public MetricsService(MeterRegistry meterRegistry) {
     this.exchangeRateRequests = Counter
        .builder("currency.exchange.rate.requests")
        .description("External currency API request attempts from CurrencyService")
        .register(meterRegistry);
  }

  public void recordExchangeRateRequest(){
    exchangeRateRequests.increment();
  }

}
