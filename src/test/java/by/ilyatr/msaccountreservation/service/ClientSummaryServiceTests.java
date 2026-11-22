package by.ilyatr.msaccountreservation.service;

import by.ilyatr.currencyclientstarter.exception.CurrencyApiClientErrorException;
import by.ilyatr.currencyclientstarter.service.CurrencyService;
import by.ilyatr.msaccountreservation.config.Settings;
import by.ilyatr.msaccountreservation.entity.Client;
import by.ilyatr.msaccountreservation.entity.enums.ClientStatus;
import by.ilyatr.msaccountreservation.exception.ClientSummaryOverloaded;
import by.ilyatr.msaccountreservation.exception.ClientSummaryTimeoutException;
import by.ilyatr.msaccountreservation.mapper.ClientMapper;
import by.ilyatr.msaccountreservation.model.ClientResponse;
import by.ilyatr.msaccountreservation.repository.AccountRepository;
import by.ilyatr.msaccountreservation.repository.ClientRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.when;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClientSummaryServiceTests {

  private static final UUID CLIENT_ID = UUID.randomUUID();
  private static final BigDecimal USD_RATE = new BigDecimal("92.50");
  private static final BigDecimal EUR_RATE = new BigDecimal("100.25");

  @Mock
  private ClientRepository clientRepository;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private ClientMapper clientMapper;

  @Mock
  private CurrencyService currencyService;

  private final ExecutorService executor = Executors.newFixedThreadPool(3);

  @AfterEach
  void tearDown() {
    executor.shutdownNow();
  }

  @Test
  void loadsClientAndRatesInParallel() throws Exception {
    Client client = activeClient();
    ClientResponse expectedClient = stubClientResponse(client);
    CountDownLatch started = new CountDownLatch(3);
    CountDownLatch release = new CountDownLatch(1);

    when(clientRepository.findById(CLIENT_ID))
        .thenAnswer(invocation -> awaitRelease(started, release, Optional.of(client)));
    when(currencyService.getExchangeRate("USD", "RUB"))
        .thenAnswer(invocation -> awaitRelease(started, release, USD_RATE));
    when(currencyService.getExchangeRate("EUR", "RUB"))
        .thenAnswer(invocation -> awaitRelease(started, release, EUR_RATE));

    var service = createService(executor, Duration.ofSeconds(10));
    var summary = service.getSummary(CLIENT_ID);

    try {
      assertThat(started.await(3, TimeUnit.SECONDS)).isTrue();
      assertThat(summary).isNotDone();
    } finally {
      release.countDown();
    }

    var response = summary.get(3, TimeUnit.SECONDS);
    assertThat(response.getClient()).isEqualTo(expectedClient);
    assertThat(response.getExchangeRates())
        .hasSize(2)
        .containsEntry("USD/RUB", USD_RATE)
        .containsEntry("EUR/RUB", EUR_RATE);
  }

  @Test
  void preservesCurrencyApiException() {
    Client client = activeClient();
    stubClientResponse(client);
    when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));

    var currencyError = new CurrencyApiClientErrorException(404, "Unknown currency");
    when(currencyService.getExchangeRate("USD", "RUB")).thenThrow(currencyError);
    when(currencyService.getExchangeRate("EUR", "RUB")).thenReturn(EUR_RATE);

    var summary = createService(executor, Duration.ofSeconds(10)).getSummary(CLIENT_ID);

    assertThatThrownBy(() -> summary.get(3, TimeUnit.SECONDS))
        .isInstanceOf(ExecutionException.class)
        .hasCause(currencyError);
  }

  @Test
  void returnsSummaryTimeoutWhenTasksStayQueued() {
    var queue = new ArrayDeque<Runnable>();
    var summary = createService(queue::add, Duration.ofMillis(100)).getSummary(CLIENT_ID);

    assertThatThrownBy(() -> summary.get(3, TimeUnit.SECONDS))
        .isInstanceOf(ExecutionException.class)
        .hasCauseInstanceOf(ClientSummaryTimeoutException.class);
  }

  @Test
  void returnsSummaryOverloadedWhenExecutorRejectsTask() {
    Executor busyExecutor = task -> {
      throw new RejectedExecutionException();
    };

    var summary = createService(busyExecutor, Duration.ofSeconds(10)).getSummary(CLIENT_ID);
    assertThatThrownBy(() -> summary.get(3, TimeUnit.SECONDS))
        .isInstanceOf(ExecutionException.class)
        .hasCauseInstanceOf(ClientSummaryOverloaded.class);
  }

  private ClientResponse stubClientResponse(Client client) {
    ClientResponse response = ClientResponse.builder()
        .id(CLIENT_ID)
        .hasAccounts(true)
        .build();

    when(accountRepository.existsByClientId(CLIENT_ID)).thenReturn(true);
    when(clientMapper.toResponse(client, true)).thenReturn(response);
    return response;
  }

  private <T> T awaitRelease(CountDownLatch started, CountDownLatch release, T value)
      throws InterruptedException {
    started.countDown();
    assertThat(release.await(5, TimeUnit.SECONDS)).isTrue();
    return value;
  }

  private Client activeClient() {
    Client client = new Client();
    client.setId(CLIENT_ID);
    client.setStatus(ClientStatus.ACTIVE);
    return client;
  }

  private ClientSummaryService createService(Executor executor, Duration timeout) {
    ClientService clientService = new ClientService(clientRepository, clientMapper, accountRepository);
    return new ClientSummaryService(
        clientService,
        currencyService,
        executor,
        new Settings(3, 3, 0, timeout)
    );
  }
}
