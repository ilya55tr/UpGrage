package by.ilyatr.msaccountreservation.service;

import by.ilyatr.msaccountreservation.entity.Client;
import by.ilyatr.msaccountreservation.entity.enums.ClientStatus;
import by.ilyatr.msaccountreservation.exception.ClientAlreadyExistsException;
import by.ilyatr.msaccountreservation.exception.ClientHasActiveAccountsException;
import by.ilyatr.msaccountreservation.exception.ClientNotFoundException;
import by.ilyatr.msaccountreservation.mapper.ClientMapper;
import by.ilyatr.msaccountreservation.model.ClientResponse;
import by.ilyatr.msaccountreservation.model.CreateClientRequest;
import by.ilyatr.msaccountreservation.model.PageClientResponse;
import by.ilyatr.msaccountreservation.model.UpdateClientRequest;
import by.ilyatr.msaccountreservation.repository.AccountRepository;
import by.ilyatr.msaccountreservation.repository.ClientRepository;
import by.ilyatr.msaccountreservation.repository.specification.ClientSpecification;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientService {

  private final ClientRepository clientRepository;
  private final ClientMapper clientMapper;
  private final AccountRepository accountRepository;

  @Transactional
  public ClientResponse create(CreateClientRequest request) {
    if (clientRepository.existsByMdmCode(request.getMdmCode())) {
      throw new ClientAlreadyExistsException(request.getMdmCode());
    }
    Client savedClient = clientRepository.saveAndFlush(
        clientMapper.toClient(request)
    );
    return clientMapper.toResponse(savedClient, hasAnyAccounts(savedClient.getId()));
  }

  @Transactional(readOnly = true)
  public PageClientResponse search(
      int page,
      int size,
      String fullName,
      Long mdmCode
  ) {
    Pageable pageable = PageRequest.of(page, size);
    Specification<Client> specification =
        ClientSpecification.notDeleted();
    if (fullName != null && !fullName.isBlank()) {
      specification = specification.and(
          ClientSpecification.fullNameContains(fullName)
      );
    }
    if (mdmCode != null) {
      specification = specification.and(
          ClientSpecification.mdmCodeEquals(mdmCode)
      );
    }
    Page<Client> clients = clientRepository.findAll(
        specification,
        pageable
    );
    return clientMapper.toPageResponse(clients);
  }

  @Transactional(readOnly = true)
  public ClientResponse getById(UUID clientId) {
    Client client = clientRepository.findById(clientId)
        .filter(c -> c.getStatus() != ClientStatus.DELETED)
        .orElseThrow(() -> new ClientNotFoundException(clientId));
    return clientMapper.toResponse(client, hasAnyAccounts(clientId));
  }

  @Transactional
  public ClientResponse update(UUID clientId, UpdateClientRequest request) {
    Client client = clientRepository.findById(clientId)
        .filter(c -> c.getStatus() != ClientStatus.DELETED)
        .orElseThrow(() -> new ClientNotFoundException(clientId));
    client = clientMapper.updateClient(request, client);
    return clientMapper.toResponse(clientRepository.saveAndFlush(client), hasAnyAccounts(clientId));
  }

  @Transactional
  public void delete(UUID clientId) {
    Client client = clientRepository.findById(clientId)
        .filter(c -> c.getStatus() != ClientStatus.DELETED)
        .orElseThrow(() -> new ClientNotFoundException(clientId));

    if (hasActiveAccounts(clientId)) {
      throw new ClientHasActiveAccountsException(
          "Client has active accounts"
      );
    }
    client.setStatus(ClientStatus.DELETED);
    clientRepository.save(client);
  }

  @Transactional(readOnly = true)
  public boolean exists(UUID clientId) {
    return clientRepository.findById(clientId)
        .map(client -> client.getStatus() != ClientStatus.DELETED)
        .orElse(false);
  }

  private boolean hasActiveAccounts(UUID clientId) {
    return accountRepository.existsByClientIdAndStatusName(clientId, "CREATED");
  }

  private boolean hasAnyAccounts(UUID clientId) {
    return accountRepository.existsByClientId(clientId);
  }

}
