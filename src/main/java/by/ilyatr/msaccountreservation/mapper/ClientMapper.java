package by.ilyatr.msaccountreservation.mapper;

import by.ilyatr.msaccountreservation.entity.Client;
import by.ilyatr.msaccountreservation.model.ClientResponse;
import by.ilyatr.msaccountreservation.model.ClientShortResponse;
import by.ilyatr.msaccountreservation.model.CreateClientRequest;
import by.ilyatr.msaccountreservation.model.PageClientResponse;
import by.ilyatr.msaccountreservation.model.PageMetadata;
import by.ilyatr.msaccountreservation.model.UpdateClientRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface ClientMapper {

  @Mapping(target = "status", constant = "ACTIVE")
  Client toClient(CreateClientRequest request);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Client updateClient(UpdateClientRequest request, @MappingTarget Client client);

  ClientResponse toResponse(Client client, boolean hasAccounts);

  ClientShortResponse toShortResponse(Client client);

  default PageClientResponse toPageResponse(Page<Client> clients){
    return PageClientResponse.builder()
        .content(
            clients.getContent().stream()
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
        ).build();
  }
}