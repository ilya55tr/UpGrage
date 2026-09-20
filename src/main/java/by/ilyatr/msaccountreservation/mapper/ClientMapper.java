package by.ilyatr.msaccountreservation.mapper;

import by.ilyatr.msaccountreservation.entity.Client;
import by.ilyatr.msaccountreservation.entity.enums.ClientStatus;
import by.ilyatr.msaccountreservation.model.ClientResponse;
import by.ilyatr.msaccountreservation.model.ClientShortResponse;
import static by.ilyatr.msaccountreservation.model.ClientStatus.valueOf;
import by.ilyatr.msaccountreservation.model.CreateClientRequest;
import by.ilyatr.msaccountreservation.model.PageClientResponse;
import by.ilyatr.msaccountreservation.model.PageMetadata;
import by.ilyatr.msaccountreservation.model.UpdateClientRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

  public Client updateClient(Client client, UpdateClientRequest request){
    client.setFullName(request.getFullName());
    client.setCitizenship(request.getCitizenship());
    client.setClientType(request.getClientType());
    client.setDocumentNumber(request.getDocumentNumber());
    client.setDocumentSeries(request.getDocumentSeries());
    client.setDocumentType(request.getDocumentType());
    return client;
  }

  public Client toClient(CreateClientRequest request){
    Client client = new Client();
    client.setFullName(request.getFullName());
    client.setCitizenship(request.getCitizenship());
    client.setClientType(request.getClientType());
    client.setDocumentNumber(request.getDocumentNumber());
    client.setDocumentSeries(request.getDocumentSeries());
    client.setDocumentType(request.getDocumentType());
    client.setMdmCode(request.getMdmCode());
    client.setStatus(ClientStatus.ACTIVE);
    return client;
  }

  public ClientResponse toResponse(Client client) {
    return ClientResponse.builder()
        .id(client.getId())
        .mdmCode(client.getMdmCode())
        .fullName(client.getFullName())
        .citizenship(client.getCitizenship())
        .clientType(client.getClientType())
        .documentNumber(client.getDocumentNumber())
        .documentSeries(client.getDocumentSeries())
        .documentType(client.getDocumentType())
        .status(valueOf(String.valueOf(client.getStatus())))
        .createdAt(client.getCreatedAt())
        .updatedAt(client.getUpdatedAt())
        .hasAccounts(false)
        .build();
  }

  public PageClientResponse toPageResponse(Page<Client> clients) {
    return PageClientResponse.builder()
        .content(
            clients.getContent()
                .stream()
                .map(this::toShortResponse)
                .toList()
        )
        .pageable(
            PageMetadata.builder()
                .pageNumber(clients.getNumber())
                .pageSize(clients.getSize())
                .totalPages(clients.getTotalPages())
                .totalElements(clients.getTotalElements())
                .build()
        )
        .build();
  }

  private ClientShortResponse toShortResponse(Client client) {

    return ClientShortResponse.builder()
        .id(client.getId())
        .mdmCode(client.getMdmCode())
        .fullName(client.getFullName())
        .status(
            valueOf(client.getStatus().name())
        )
        .build();
  }
}
