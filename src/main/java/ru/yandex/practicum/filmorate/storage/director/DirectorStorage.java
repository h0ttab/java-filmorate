package ru.yandex.practicum.filmorate.storage.director;

import java.util.List;
import java.util.Set;

import ru.yandex.practicum.filmorate.model.Director;

public interface DirectorStorage {
    Director create(Director director);

    List<Director> findAll();

    List<Director> findByFilm(Integer filmId);

    Director findById(Integer directorId);

    Director update(Director director);

    void linkDirectorsToFilm(Integer filmId, Set<Integer> directorIds, boolean clearExisting);

    void delete(Integer directorId);
}
