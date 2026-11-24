package cl.martini.identity.model;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id @Column(length = 36)
    private String id;
    @Column(nullable = false, length = 254, unique = true)
    private String email;
    @Column(nullable = false, length = 80, unique = true)
    private String username;
    @Column(nullable = false, length = 200)
    private String passwordHash;
    @Column(nullable = false, length = 8)
    private String role;
    @Column(nullable = false, length = 200)
    private String nombre;
    private String nombreNegocio;
    private String tipoNegocio;
    private String empleados;
    private String telefono;
    private String direccion;
    private String rutEmpresa;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<String> intereses = new ArrayList<>();
}
