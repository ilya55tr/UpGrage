package by.ilyatr.currencyclientstarter.env;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;

public class CurrencyClientEnvironmentPostProcessor implements EnvironmentPostProcessor {

  private static final String FILE = "currency-client-starter.properties";

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    Resource resource = new ClassPathResource(FILE);
    if (!resource.exists()){
      return;
    }
    try{
      environment.getPropertySources().addLast(new ResourcePropertySource("currency-client-starter", resource));
    }catch (IOException e){
      throw new UncheckedIOException("Failed to load "+ FILE, e);
    }
  }
}
