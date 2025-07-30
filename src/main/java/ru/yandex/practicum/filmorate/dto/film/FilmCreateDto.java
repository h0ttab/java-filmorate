package ru.yandex.practicum.filmorate.dto.film;

import java.time.LocalDate;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
public class FilmCreateDto {
    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @Length(max = 200)
    private String description;

    @NotNull
    private LocalDate releaseDate;

    @NotNull
    @Positive
    private Integer duration;
}