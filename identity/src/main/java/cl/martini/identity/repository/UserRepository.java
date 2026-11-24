package cl.martini.identity.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import cl.martini.identity.model.User;
import org.springframework.data.jpa.repository.*;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findForUpdate(String id);
}
