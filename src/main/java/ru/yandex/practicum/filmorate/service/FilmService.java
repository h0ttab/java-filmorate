package ru.yandex.practicum.filmorate.service;

import java.util.Collection;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExceptionType;
import ru.yandex.practicum.filmorate.exception.LoggedException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.model.dto.film.FilmUpdateDto;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.SortOrder;
import ru.yandex.practicum.filmorate.util.DtoHelper;
import ru.yandex.practicum.filmorate.util.Validators;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final FilmMapper filmMapper;
    private final LikeService likeService;
    private final DtoHelper dtoHelper;
    private final Validators validators;

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Integer filmId) {
        return filmStorage.findById(filmId);
    }

    /**
     * Находит топ N фильмов по количеству лайков с возможностью фильтрации по жанру и году выпуска
     *
     * @param count   количество фильмов для вывода
     * @param genreId идентификатор жанра для фильтрации (может быть null)
     * @param year    год выпуска для фильтрации (может быть null)
     * @return список фильмов, отсортированных по количеству лайков (по убыванию)
     */
    public List<Film> findTopLiked(int count, Integer genreId, Integer year) {
        // Проверка существования жанра, если он указан
        if (genreId != null) {
            validators.validateGenreExists(genreId, getClass());
        }

        if (genreId == null && year == null) {
            // Если не указаны дополнительные параметры, используем существующий метод
            return filmStorage.findTopLiked(count);
        }

        return filmStorage.findTopLiked(count, genreId, year);
    }

    public List<Film> findTopLiked(int count) {
        return filmStorage.findTopLiked(count);
    }

    public List<Film> findByDirector(Integer directorId, String sortOrder) {
        try {
            SortOrder order = SortOrder.valueOf(sortOrder.toUpperCase());
            validators.validateDirectorExists(directorId, getClass());
            return filmStorage.findByDirector(directorId, order);
        } catch (IllegalArgumentException e) {
            LoggedException.throwNew(ExceptionType.INVALID_SORT_ORDER, getClass(), List.of());
        }
        return List.of();
    }

    public Film create(FilmCreateDto filmCreateDto) {
        Film film = filmMapper.toEntity(filmCreateDto);
        return filmStorage.create(film);
    }

    public Film update(FilmUpdateDto filmUpdateDto) {
        validators.validateFilmExists(filmUpdateDto.getId(), getClass());

        Film filmUpdate = filmMapper.toEntity(filmUpdateDto);
        Film filmOriginal = filmStorage.findById(filmUpdate.getId());

        filmUpdate.getDirectors()
                .forEach(director -> validators.validateDirectorExists(director.getId(), getClass()));
        validators.validateFilmReleaseDate(filmUpdateDto.getReleaseDate(), getClass());
        validators.validateFilmDescription(filmUpdateDto.getDescription(), filmUpdateDto.getId(), getClass());

        filmUpdate = (Film) dtoHelper.transferFields(filmOriginal, filmUpdate);
        return filmStorage.update(filmUpdate);
    }

    public void addLike(Integer filmId, Integer userId) {
        validators.validateFilmExists(filmId, getClass());
        validators.validateUserExists(userId, getClass());
        likeService.addLike(filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        validators.validateFilmExists(filmId, getClass());
        validators.validateUserExists(userId, getClass());
        likeService.removeLike(filmId, userId);
    }

    public void delete(Integer filmId) {
        filmStorage.delete(filmId);
    }
}