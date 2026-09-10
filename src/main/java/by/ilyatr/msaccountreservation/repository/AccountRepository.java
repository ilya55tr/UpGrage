package by.ilyatr.msaccountreservation.repository;

import by.ilyatr.msaccountreservation.entity.Account;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {
}