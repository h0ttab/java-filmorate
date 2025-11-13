package ru.yandex.practicum.filmorate.mapper;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.dto.user.UserCreateDto;
import ru.yandex.practicum.filmorate.model.dto.user.UserUpdateDto;
import ru.yandex.practicum.filmorate.util.Validators;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final Validators validators;

    public User toEntity(UserCreateDto userCreateDto) {
        return User.builder()
                .name(validators.isValidString(userCreateDto.getName()) ? userCreateDto.getName() : userCreateDto.getLogin())
                .login(userCreateDto.getLogin())
                .email(userCreateDto.getEmail())
                .birthday(userCreateDto.getBirthday())
                .build();
    }

    public User toEntity(UserUpdateDto userUpdateDto) {
        Optional<String> userEmail = Optional.ofNullable(userUpdateDto.getEmail());
        Optional<String> userName = Optional.ofNullable(userUpdateDto.getName());
        return User.builder()
                .id(userUpdateDto.getId())
                .email(userEmail.orElse(null))
                .name(validators.isValidString(userName.orElse(null)) ? userName.get() : userUpdateDto.getLogin()
                )
                .login(userUpdateDto.getLogin())
                .birthday(userUpdateDto.getBirthday())
                .build();
    }
}
