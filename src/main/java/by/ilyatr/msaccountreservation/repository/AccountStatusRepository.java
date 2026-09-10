package by.ilyatr.msaccountreservation.repository;

import by.ilyatr.msaccountreservation.entity.AccountStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountStatusRepository extends JpaRepository<AccountStatus, Integer> {

}
