package ru.yandex.practicum.filmorate.model.dto.film;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DirectorDto {
    @NotNull
    @NotBlank
    private String name;
}
