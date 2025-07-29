package ru.yandex.practicum.filmorate.dto.put;

import java.time.LocalDate;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPutDto {
    private Integer id;

    @Email
    @NotNull
    private String email;

    @NotNull
    @NotBlank
    private String login;

    private String name;

    @PastOrPresent
    @NotNull
    private LocalDate birthday;
}