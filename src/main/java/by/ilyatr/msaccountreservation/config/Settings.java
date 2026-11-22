package by.ilyatr.msaccountreservation.config;

import java.time.Duration;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.async")
@Getter
public class Settings {
  private final int coreSize;
  private final int maxSize;
  private final int queueCapacity;
  private final Duration summaryTimeout;

  public Settings(int coreSize, int maxSize, int queueCapacity, Duration summaryTimeout) {
    this.coreSize = coreSize;
    this.maxSize = maxSize;
    this.queueCapacity = queueCapacity;
    this.summaryTimeout = summaryTimeout;

    if (coreSize < 2 || maxSize < coreSize || queueCapacity < 0) {
      throw new IllegalArgumentException("Invalid app.async pool settings");
    }
    if (summaryTimeout == null || summaryTimeout.toMillis() < 1) {
      throw new IllegalArgumentException("app.async.summary-timeout must be at least 1ms");
    }
  }
}
