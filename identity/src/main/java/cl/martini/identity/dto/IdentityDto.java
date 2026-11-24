package cl.martini.identity.dto;

import java.util.List;
import jakarta.validation.constraints.*;

public final class IdentityDto {
    private IdentityDto() {}

    public record Register(
            @NotBlank @Size(max = 200) String nombre,
            @NotBlank @Email @Size(max = 254) String correo,
            @NotBlank @Size(min = 3, max = 80) String usuario,
            @NotBlank @Size(min = 8, max = 128) String password,
            @AssertTrue boolean privacidad, @AssertTrue boolean terminos,
            @Size(max = 200) String nombreNegocio, @Size(max = 200) String tipoNegocio,
            @Size(max = 200) String empleados, @Size(max = 200) String telefono,
            @Size(max = 200) String direccion, @Size(max = 200) String rutEmpresa,
            @Size(max = 30) List<@NotBlank @Size(max = 100) String> intereses) {

        public ProfilePatch details() {
            return new ProfilePatch(nombre, correo, usuario, nombreNegocio, tipoNegocio,
                    empleados, telefono, direccion, rutEmpresa, intereses);
        }
    }

    public record Login(@NotBlank @Email @Size(max = 254) String correo,
                        @NotBlank @Size(min = 8, max = 128) String password) {}

    public record ProfilePatch(
            @Size(min = 1, max = 200) String nombre, @Email @Size(max = 254) String correo,
            @Size(min = 3, max = 80) String usuario, @Size(max = 200) String nombreNegocio,
            @Size(max = 200) String tipoNegocio, @Size(max = 200) String empleados,
            @Size(max = 200) String telefono, @Size(max = 200) String direccion,
            @Size(max = 200) String rutEmpresa,
            @Size(max = 30) List<@NotBlank @Size(max = 100) String> intereses) {}

    public record Profile(String id, String role, String nombre, String correo, String usuario,
                          String nombreNegocio, String tipoNegocio, String empleados, String telefono,
                          String direccion, String rutEmpresa, List<String> intereses) {}

    public record Session(String accessToken, long expiresIn, Profile user) {}
}
