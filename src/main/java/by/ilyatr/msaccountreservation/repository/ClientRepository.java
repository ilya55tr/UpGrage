package by.ilyatr.msaccountreservation.repository;

import by.ilyatr.msaccountreservation.entity.Client;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClientRepository extends JpaRepository<Client, UUID>, JpaSpecificationExecutor<Client> {
  boolean existsByMdmCode(Long mdmCode);
}