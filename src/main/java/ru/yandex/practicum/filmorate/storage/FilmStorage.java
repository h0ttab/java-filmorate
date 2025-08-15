package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;
import java.util.Map;

import ru.yandex.practicum.filmorate.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.model.Film;

public interface FilmStorage {
    Map<Integer, Film> getStorage();

    Collection<Film> findAll();

    Film findById(Integer filmId);

    Film create(FilmCreateDto filmCreateDto);

    Film update(Film filmUpdate, Film filmOriginal);

    Integer delete(Integer filmId);
}
