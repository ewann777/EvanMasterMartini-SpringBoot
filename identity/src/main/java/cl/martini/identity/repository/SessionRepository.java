package cl.martini.identity.repository;

import java.util.Optional;
import cl.martini.identity.model.LoginSession;
import org.springframework.data.jpa.repository.*;

public interface SessionRepository extends JpaRepository<LoginSession, String> {
    Optional<LoginSession> findByTokenHashAndExpiresAtGreaterThan(String tokenHash, long time);

    @Modifying
    @Query("delete from LoginSession s where s.expiresAt <= :time")
    void deleteExpired(long time);

    @Modifying
    @Query("delete from LoginSession s where s.tokenHash = :hash")
    void revoke(String hash);
}
