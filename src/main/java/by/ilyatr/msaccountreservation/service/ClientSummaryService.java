package by.ilyatr.msaccountreservation.service;

import by.ilyatr.currencyclientstarter.service.CurrencyService;
import by.ilyatr.msaccountreservation.config.ClientSummaryAsyncConfig;
import by.ilyatr.msaccountreservation.config.Settings;
import by.ilyatr.msaccountreservation.exception.ClientSummaryOverloaded;
import by.ilyatr.msaccountreservation.exception.ClientSummaryTimeoutException;
import by.ilyatr.msaccountreservation.model.ClientSummaryResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ClientSummaryService {

  private final ClientService clientService;
  private final CurrencyService currencyService;
  private final Executor executor;
  private final long timeoutMillis;

  public ClientSummaryService(
      ClientService clientService,
      CurrencyService currencyService,
      @Qualifier(ClientSummaryAsyncConfig.EXECUTOR) Executor executor,
      Settings settings
  ) {
    this.clientService = clientService;
    this.currencyService = currencyService;
    this.executor = executor;
    this.timeoutMillis = settings.getSummaryTimeout().toMillis();
  }

  public CompletableFuture<ClientSummaryResponse> getSummary(UUID clientId) {
    try {
      var client = CompletableFuture.supplyAsync(
          () -> clientService.getById(clientId), executor);
      var usd = CompletableFuture.supplyAsync(
          () -> currencyService.getExchangeRate("USD", "RUB"), executor);
      var eur = CompletableFuture.supplyAsync(
          () -> currencyService.getExchangeRate("EUR", "RUB"), executor);

      return CompletableFuture.allOf(client, usd, eur)
          .orTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
          .thenApply(ignored -> ClientSummaryResponse.builder()
              .client(client.join())
              .exchangeRates(Map.of("USD/RUB", usd.join(), "EUR/RUB", eur.join()))
              .build())
          .exceptionallyCompose(this::translateFailure);
    } catch (RejectedExecutionException exception) {
      return CompletableFuture.failedFuture(new ClientSummaryOverloaded());
    }
  }

  private CompletableFuture<ClientSummaryResponse> translateFailure(Throwable failure) {
    if (failure.getCause() instanceof TimeoutException) {
      return CompletableFuture.failedFuture(new ClientSummaryTimeoutException());
    }
    return CompletableFuture.failedFuture(failure.getCause());
  }
}
