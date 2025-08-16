package ru.yandex.practicum.filmorate.controller;

import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.user.UserCreateDto;
import ru.yandex.practicum.filmorate.dto.user.UserUpdateDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Integer id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public String addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.addFriend(id, friendId);
        return String.format("Пользователь id %d добавлен в список друзей пользователя id %d", friendId, id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public String removeFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.removeFriend(id, friendId);
        return String.format("Пользователь id %d удалён из списка друзей пользователя id %d", friendId, id);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getUserFriends(@PathVariable Integer id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getMutualFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        return userService.findMutualFriends(id, otherId);
    }

    @PostMapping
    public User create(@Valid @NotNull @RequestBody UserCreateDto userCreateDto) {
        return userService.create(userCreateDto);
    }

    @PutMapping
    public User update(@RequestBody UserUpdateDto userUpdateDto) {
        return userService.update(userUpdateDto);
    }
}
