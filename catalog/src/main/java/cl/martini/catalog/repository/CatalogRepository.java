package cl.martini.catalog.repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import cl.martini.catalog.model.CatalogEntry;
import org.springframework.data.jpa.repository.*;

public interface CatalogRepository extends JpaRepository<CatalogEntry, Long> {
    List<CatalogEntry> findByKindOrderBySequenceId(String kind);
    Optional<CatalogEntry> findByKindAndSlug(String kind, String slug);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from CatalogEntry e where e.kind = :kind and e.slug = :slug")
    Optional<CatalogEntry> findForUpdate(String kind, String slug);
}
