package ru.yandex.practicum.filmorate.service;

import java.util.*;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.util.Validators;

@Service
@RequiredArgsConstructor
public class DirectorService {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final DirectorStorage directorStorage;
    private final Validators validators;

    public Director create(Director director) {
        Director createdDirector = directorStorage.create(director);
        log.info("Добавлен режиссёр {}", createdDirector);
        return createdDirector;
    }

    public List<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director findById(Integer directorId) {
        validators.validateDirectorExists(directorId, getClass());
        return directorStorage.findById(directorId);
    }

    public List<Director> findByFilm(Integer filmId) {
        validators.validateFilmExists(filmId, getClass());
        return directorStorage.findByFilm(filmId);
    }

    public void linkDirectorToFilm(Integer filmId, List<Integer> directorIds, boolean clearExisting) {
        directorStorage.linkDirectorsToFilm(filmId, new HashSet<>(directorIds), clearExisting);
    }

    public Director update(Director director) {
        Director updatedDirector = directorStorage.update(director);
        log.info("Обновлена информация о режиссёре id={}. Новое значение: {}",
                updatedDirector.getId(), updatedDirector);
        return updatedDirector;
    }

    public void delete(Integer directorId) {
        directorStorage.delete(directorId);
        log.info("Удален режиссёр id={}", directorId);
    }
}
