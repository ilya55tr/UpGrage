package by.ilyatr.msaccountreservation.config;


import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableConfigurationProperties(Settings.class)
public class ClientSummaryAsyncConfig {

  public static final String EXECUTOR = "clientSummaryExecutor";

  @Bean(name = EXECUTOR, defaultCandidate = false)
  public ThreadPoolTaskExecutor clientSummaryExecutor(Settings settings){
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(settings.getCoreSize());
    executor.setMaxPoolSize(settings.getMaxSize());
    executor.setQueueCapacity(settings.getQueueCapacity());
    executor.setThreadNamePrefix("client-summary-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    return executor;
  }
}
