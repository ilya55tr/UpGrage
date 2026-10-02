package by.ilyatr.currencyclientstarter.autoconfigure;

import by.ilyatr.currencyclientstarter.properties.CurrencyClientProperties;
import by.ilyatr.currencyclientstarter.service.CurrencyService;
import by.ilyatr.currencyclientstarter.service.CurrencyServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@EnableConfigurationProperties(CurrencyClientProperties.class)
public class CurrencyClientAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(name = "currencyRestTemplate")
  public RestTemplate currencyRestTemplate(CurrencyClientProperties properties) {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(properties.getConnectTimeout());
    factory.setReadTimeout(properties.getReadTimeout());
    return new RestTemplate(factory);
  }

  @Bean
  @ConditionalOnMissingBean(CurrencyService.class)
  public CurrencyService currencyService(RestTemplate currencyRestTemplate, CurrencyClientProperties properties) {
    return new CurrencyServiceImpl(
        currencyRestTemplate,
        properties
    );
  }
}
