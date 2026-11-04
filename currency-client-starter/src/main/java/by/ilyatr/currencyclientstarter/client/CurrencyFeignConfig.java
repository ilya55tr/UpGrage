package by.ilyatr.currencyclientstarter.client;

import by.ilyatr.currencyclientstarter.properties.CurrencyClientProperties;
import feign.Logger;
import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;


public class CurrencyFeignConfig {

  @Bean
  public Request.Options currencyRequestOptions(CurrencyClientProperties properties){
    var timeout = properties.getTimeout();
    return new Request.Options(timeout.getConnectTimeout(), timeout.getReadTimeout(), true);
  }

  @Bean
  public Logger.Level currencyLoggerLevel(CurrencyClientProperties properties){
    return properties.getLoggerLevel();
  }

  @Bean
  public ErrorDecoder currencyErrorDecoder(){
    return new CurrencyErrorDecoder();
  }
}
