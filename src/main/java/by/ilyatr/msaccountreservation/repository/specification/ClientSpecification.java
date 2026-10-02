package by.ilyatr.msaccountreservation.repository.specification;

import by.ilyatr.msaccountreservation.entity.Client;
import by.ilyatr.msaccountreservation.entity.enums.ClientStatus;
import org.springframework.data.jpa.domain.Specification;

public final class ClientSpecification {

  private ClientSpecification() {
  }

  public static Specification<Client> fullNameContains(String fullName) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.like(
            criteriaBuilder.lower(root.get("fullName")),
            "%" + fullName.toLowerCase() + "%"
        );
  }

  public static Specification<Client> mdmCodeEquals(Long mdmCode) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(
            root.get("mdmCode"),
            mdmCode
        );
  }

  public static Specification<Client> notDeleted() {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.notEqual(
            root.get("status"),
            ClientStatus.DELETED
        );
  }
}