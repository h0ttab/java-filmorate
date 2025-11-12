package ru.yandex.practicum.filmorate.controller;

import java.util.Collection;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.model.dto.film.FilmUpdateDto;
import ru.yandex.practicum.filmorate.service.FilmService;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Integer id) {
        System.out.println("Отправлен запрос на получение фильма11: " + id);
        return filmService.findById(id);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> findByDirector(@PathVariable Integer directorId, @RequestParam String sortBy) {
        return filmService.findByDirector(directorId, sortBy);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(@PathVariable Integer filmId, @PathVariable Integer userId) {
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> findTopLiked(@RequestParam(required = false, defaultValue = "10") int count) {
        return filmService.findTopLiked(count);
    }

    @PostMapping
    public Film create(@Valid @NotNull @RequestBody FilmCreateDto filmCreateDto) {
        return filmService.create(filmCreateDto);
    }

    @PutMapping
    public Film update(@Valid @RequestBody FilmUpdateDto filmUpdateDto) {
        return filmService.update(filmUpdateDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        filmService.delete(id);
    }
}
