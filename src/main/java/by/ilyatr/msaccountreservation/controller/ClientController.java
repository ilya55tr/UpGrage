package by.ilyatr.msaccountreservation.controller;

import by.ilyatr.msaccountreservation.api.ClientsApi;
import by.ilyatr.msaccountreservation.model.ClientExistsResponse;
import by.ilyatr.msaccountreservation.model.ClientResponse;
import by.ilyatr.msaccountreservation.model.CreateClientRequest;
import by.ilyatr.msaccountreservation.model.PageClientResponse;
import by.ilyatr.msaccountreservation.model.UpdateClientRequest;
import by.ilyatr.msaccountreservation.service.ClientService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ClientController implements ClientsApi {

  private final ClientService clientService;

  @Override
  public ResponseEntity<ClientResponse> createClient(@Valid CreateClientRequest request) {
    ClientResponse response = clientService.create(request);
    return ResponseEntity
        .status(201)
        .body(response);
  }

  @Override
  public ResponseEntity<PageClientResponse> searchClients(
      Integer page,
      Integer size,
      String fullName,
      Long mdmCode
  ) {
    return ResponseEntity.ok(clientService.search(page, size, fullName, mdmCode));
  }

  @Override
  public ResponseEntity<ClientResponse> getClientById(UUID clientId) {
    return ResponseEntity.ok(
        clientService.getById(clientId)
    );
  }

  @Override
  public ResponseEntity<ClientResponse> updateClient(UUID clientId, @Valid UpdateClientRequest request) {
    return ResponseEntity.ok(
        clientService.update(clientId, request)
    );
  }

  @Override
  public ResponseEntity<Void> deleteClient(UUID clientId) {
    clientService.delete(clientId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<ClientExistsResponse> clientExists(UUID clientId) {
    boolean exists = clientService.exists(clientId);
    ClientExistsResponse response = new ClientExistsResponse();
    response.setExists(exists);
    response.setClientId(clientId);
    if (exists) {
      response.setStatus(
          clientService.getById(clientId).getStatus()
      );
    }
    return ResponseEntity.ok(response);
  }
}
