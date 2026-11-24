package cl.martini.contact.dto;

import jakarta.validation.constraints.*;

public final class ContactDto {
    private ContactDto() {}

    public record Create(@NotBlank @Size(min = 3, max = 120) String subject,
                         @NotBlank @Size(min = 10, max = 3000) String body) {}

    public record Status(@NotNull @Pattern(regexp = "recibida|revisada|cerrada") String status) {}

    public record Message(long number, String subject, String body, String status, String createdAt) {}

    public record AdminMessage(long number, String subject, String body, String status, String createdAt, String userId) {}
}
