package by.ilyatr.currencyclientstarter.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.currency-client")
@Getter
@Setter
public class CurrencyClientProperties {

  /**
   * Base URL внешнего API.
   */
  private String baseUrl = "https://api.frankfurter.dev";

  /**
   * Timeout подключения в миллисекундах.
   */
  private int connectTimeout = 3000;

  /**
   * Timeout чтения в миллисекундах.
   */
  private int readTimeout = 5000;
}
