package by.ilyatr.currencyclientstarter.properties;

import feign.Logger;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.currency-client")
@Getter
@Setter
public class CurrencyClientProperties {

  private boolean enabled = false;

  private String baseUrl = "https://api.frankfurter.dev";

  private String ratePath = "/v2/rate";

  private Logger.Level loggerLevel = Logger.Level.BASIC;

  private final TimeoutProperties timeout = new TimeoutProperties();
  private final RetryProperties retry = new RetryProperties();
  private final RateLimiterProperties rateLimiter = new RateLimiterProperties();

  @Getter
  @Setter
  public static class TimeoutProperties {
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(5);
  }

  @Getter
  @Setter
  public static class RetryProperties {
    private int maxAttempts = 3;
    private Duration waitDuration = Duration.ofMillis(500);
  }

  @Getter
  @Setter
  public static class RateLimiterProperties {
    private int limitForPeriod = 10;
    private Duration limitRefreshPeriod = Duration.ofSeconds(1);
    private Duration timeoutDuration = Duration.ZERO;
  }

  @Getter
  @Setter
  public static class HealthProperties {
    private boolean enabled = true;
  }
}
