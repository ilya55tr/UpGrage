package by.ilyatr.currencyclientstarter.health;

import by.ilyatr.currencyclientstarter.client.CurrencyFeignClient;
import by.ilyatr.currencyclientstarter.properties.CurrencyClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;


@RequiredArgsConstructor
public class CurrencyApiHealthIndicator implements HealthIndicator {

  private final CurrencyFeignClient client;
  private final CurrencyClientProperties properties;

  @Override
  public Health health() {
    long start = System.currentTimeMillis();
    try {
      client.getRate("usd", "eur");
      return Health.up()
          .withDetail("baseUrl", properties.getBaseUrl())
          .withDetail("responseTimeMs", System.currentTimeMillis() - start)
          .build();
    } catch (Exception e){
      return Health.down(e)
          .withDetail("baseUrl", properties.getBaseUrl())
          .build();
    }
  }
}
