package ru.yandex.practicum.filmorate.controller;

import java.util.Collection;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.film.FilmUpdateDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Integer id) {
        return filmService.findById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public String addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.addLike(id, userId);
        return String.format("Пользователь id %d поставил лайк фильму id %d", userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public String removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.removeLike(id, userId);
        return String.format("Пользователь id %d убрал свой лайк с фильма id %d", userId, id);
    }

    @GetMapping("/popular?count={count}")
    public List<Film> findTopLiked(
            @RequestParam(required = false, defaultValue = "10")
            int count
    ) {
        return filmService.findTopLiked(count);
    }

    @PostMapping
    public Film create(@Valid @NotNull @RequestBody FilmCreateDto filmCreateDto) {
        return filmService.create(filmCreateDto);
    }

    @PutMapping
    public Film update(@RequestBody FilmUpdateDto filmUpdateDto) {
        return filmService.update(filmUpdateDto);
    }

}
