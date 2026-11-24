package cl.martini.contact.repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import cl.martini.contact.model.ContactMessage;
import org.springframework.data.jpa.repository.*;

public interface ContactRepository extends JpaRepository<ContactMessage, Long> {
    List<ContactMessage> findByUserIdOrderByNumberDesc(String userId);
    List<ContactMessage> findAllByOrderByNumberDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from ContactMessage m where m.number = :number")
    Optional<ContactMessage> findForUpdate(long number);
}
