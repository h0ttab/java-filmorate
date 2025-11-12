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
public class FilmUpdateDto {
    @NotNull
    @Positive
    private Integer id;

    @NotBlank
    private Optional<String> name;

    @Length(max = 200)
    private Optional<String> description;

    private LocalDate releaseDate;

    @Positive
    private Optional<Integer> duration;

    @NotEmpty
    private Optional<List<ObjectIdDto>> genres;

    @Positive
    private Optional<ObjectIdDto> mpa;

    private Optional<List<ObjectIdDto>> directors;
}