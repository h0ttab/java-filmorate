package ru.yandex.practicum.filmorate.storage.genre;

import java.util.List;
import java.util.Set;

import ru.yandex.practicum.filmorate.model.Genre;

public interface GenreStorage {
    List<Genre> findAll();

    Genre findById(Integer genreId);

    List<Genre> findGenreByFilmId(Integer filmId);

    void linkGenresToFilm(Integer filmId, Set<Integer> genreIdSet, boolean clearExisting);
}
