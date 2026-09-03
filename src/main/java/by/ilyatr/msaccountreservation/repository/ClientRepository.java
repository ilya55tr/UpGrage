package by.ilyatr.msaccountreservation.repository;

import by.ilyatr.msaccountreservation.entity.Client;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, UUID> {
}