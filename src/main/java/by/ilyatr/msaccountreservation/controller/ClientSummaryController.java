package by.ilyatr.msaccountreservation.controller;

import by.ilyatr.msaccountreservation.api.ClientSummaryApi;
import by.ilyatr.msaccountreservation.model.ClientSummaryResponse;
import by.ilyatr.msaccountreservation.service.ClientSummaryService;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ClientSummaryController implements ClientSummaryApi {

  private final ClientSummaryService clientSummaryService;

  @Override
  public CompletableFuture<ResponseEntity<ClientSummaryResponse>> getClientSummary(UUID clientId) {
    return clientSummaryService.getSummary(clientId)
        .thenApply(ResponseEntity::ok);
  }
}
