package ru.yandex.practicum.filmorate.model.dto.film;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.model.dto.ObjectIdDto;

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

    private Optional<List<ObjectIdDto>> genres;

    private Optional<ObjectIdDto> mpa;

    private Optional<List<ObjectIdDto>> directors;
}