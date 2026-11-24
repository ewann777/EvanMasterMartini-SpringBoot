package cl.martini.catalog.dto;

import java.util.List;
import jakarta.validation.constraints.*;

public final class CatalogDto {
    private CatalogDto() {}

    public record Recipe(
            @NotBlank @Size(max = 200) String title, @NotBlank @Size(max = 2000) String description,
            @NotBlank @Size(max = 2000) String duration, @NotBlank @Size(max = 2000) String servings,
            @NotEmpty @Size(max = 100) List<@NotBlank @Size(max = 2000) String> ingredients,
            @NotEmpty @Size(max = 100) List<@NotBlank @Size(max = 2000) String> steps,
            @NotNull @Size(max = 2000) String videoTitle, @Size(max = 500) String videoUrl,
            @NotBlank @Size(max = 2000) String category, @NotNull Boolean published) {}

    public record Learning(
            @NotBlank @Size(max = 200) String title, @NotBlank @Size(max = 2000) String specialty,
            @NotBlank @Size(max = 2000) String summary, @NotNull Boolean isVideo,
            @NotEmpty @Size(max = 100) List<@NotBlank @Size(max = 2000) String> lessons,
            @Size(max = 500) String videoUrl, @NotNull Boolean published) {}
}
