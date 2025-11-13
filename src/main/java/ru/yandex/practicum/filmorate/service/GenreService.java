package ru.yandex.practicum.filmorate.service;

import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExceptionType;
import ru.yandex.practicum.filmorate.exception.LoggedException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.util.Validators;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage genreStorage;
    private final Validators validators;

    public List<Genre> findAll() {
        return genreStorage.findAll();
    }

    public Genre findById(Integer genreId) {
        validators.validateGenreExists(genreId, getClass());
        return genreStorage.findById(genreId);
    }

    public List<Genre> findByFilmId(Integer filmId) {
        return genreStorage.findGenreByFilmId(filmId);
    }

    public List<Genre> findByIdList(List<Integer> idList) {
        List<Genre> genreList = genreStorage.findByIdList(idList);
        if (genreList.isEmpty()) {
            LoggedException.throwNew(ExceptionType.GENRE_NOT_FOUND, getClass(), idList);
        }
        return genreList;
    }

    public void linkGenresToFilm(Integer filmId, Set<Integer> genreIdSet, boolean clearExisting) {
        genreStorage.linkGenresToFilm(filmId, genreIdSet, clearExisting);
    }
}
