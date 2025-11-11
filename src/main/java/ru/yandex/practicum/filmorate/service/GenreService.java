package ru.yandex.practicum.filmorate.service;

import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.util.Validators;

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

    public List<Genre> findGenreByFilmId(Integer filmId) {
        return genreStorage.findGenreByFilmId(filmId);
    }

    public void linkGenresToFilm(Integer filmId, Set<Integer> genreIdSet, boolean clearExisting) {
        genreStorage.linkGenresToFilm(filmId, genreIdSet, clearExisting);
    }
}
