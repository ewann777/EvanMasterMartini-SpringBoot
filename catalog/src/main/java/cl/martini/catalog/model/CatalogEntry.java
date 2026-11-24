package cl.martini.catalog.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "catalog_entries")
@Getter
@NoArgsConstructor
public class CatalogEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sequenceId;
    @Column(nullable = false, length = 16)
    private String kind;
    @Column(nullable = false, length = 80)
    private String slug;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private JsonNode payload;

    public CatalogEntry(String kind, String slug, JsonNode payload) {
        this.kind = kind;
        this.slug = slug;
        this.payload = payload;
    }

    public void replacePayload(JsonNode payload) {
        this.payload = payload;
    }
}
