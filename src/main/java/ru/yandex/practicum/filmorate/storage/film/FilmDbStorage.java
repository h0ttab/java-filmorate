package ru.yandex.practicum.filmorate.storage.film;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ExceptionType;
import ru.yandex.practicum.filmorate.exception.LoggedException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.*;

@Primary
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorage implements FilmStorage {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper mapper;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final LikeService likeService;
    private final DirectorService directorService;

    private Film addAllAttributesToFilm(Film film) {
        Integer filmId = film.getId();
        Integer mpaId = film.getMpa().getId();

        Mpa mpa = mpaService.findById(mpaId);
        List<Genre> genre = genreService.findGenreByFilmId(filmId);
        List<Integer> likes = likeService.getLikesByFilmId(filmId);
        List<Director> directors = directorService.findByFilm(filmId);
        film.setMpa(mpa);
        film.setGenres(genre);
        film.getLikes().addAll(likes);
        film.setDirectors(directors);
        return film;
    }

    @Override
    public List<Film> findAll() {
        String query = "SELECT * FROM film;";
        List<Film> films = jdbcTemplate.query(query, mapper);
        films.forEach(this::addAllAttributesToFilm);
        return films;
    }

    @Override
    public Film findById(Integer filmId) {
        String query = "SELECT * FROM film WHERE id = ?;";
        List<Film> result = jdbcTemplate.query(query, mapper, filmId);
        if (result.isEmpty()) {
            LoggedException.throwNew(ExceptionType.FILM_NOT_FOUND, getClass(), List.of(filmId));
        }
        Film film = result.getFirst();
        addAllAttributesToFilm(film);
        return film;
    }

    @Override
    public Film create(Film film) {
        String query = """
                INSERT INTO film (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID)
                VALUES(?, ?, ?, ?, ?);
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(query, new String[]{"id"});
                    ps.setString(1, film.getName());
                    ps.setString(2, film.getDescription());
                    ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                    ps.setInt(4, film.getDuration());
                    ps.setInt(5, film.getMpa().getId());
                    return ps;
                }, keyHolder);

        if (Optional.ofNullable(keyHolder.getKey()).isEmpty()) {
            LoggedException.throwNew(ExceptionType.UNEXPECTED_ERROR, getClass(), List.of());
        }

        film.setId(keyHolder.getKey().intValue());
        log.info("Добавлен новый фильм: {}", film);
        List<Integer> directors = film.getDirectors().stream().mapToInt(Director::getId).boxed().toList();
        directorService.linkDirectorToFilm(film.getId(), directors, false);
        genreService.linkGenresToFilm(film.getId(), extractGenreIdSet(film), false);
        addAllAttributesToFilm(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String queryFilmUpdate = """
                    UPDATE film
                    SET name = ?,
                        description = ?,
                        release_date = ?,
                        duration = ?,
                        mpa_id = ?
                    WHERE film.id = ?;
                """;
        int updatedFilmRows = jdbcTemplate.update(
                queryFilmUpdate,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        if (updatedFilmRows == 0) {
            LoggedException.throwNew(ExceptionType.FILM_NOT_FOUND, getClass(), List.of(film.getId()));
        }
        log.info("Обновлён фильм id {}. Новое значение: {}", film.getId(), film);
        List<Integer> directors = film.getDirectors().stream().mapToInt(Director::getId).boxed().toList();
        genreService.linkGenresToFilm(film.getId(), extractGenreIdSet(film), true);
        directorService.linkDirectorToFilm(film.getId(), directors, true);
        addAllAttributesToFilm(film);
        return film;
    }

    @Override
    public Integer delete(Integer filmId) {
        String query = "DELETE FROM film WHERE id = ?";
        int deletedRows = jdbcTemplate.update(query, filmId);
        if (deletedRows == 0) {
            LoggedException.throwNew(ExceptionType.FILM_NOT_FOUND, getClass(), List.of(filmId));
        }
        log.info("Удалён фильм id {}", filmId);
        return filmId;
    }

    @Override
    public List<Film> findTopLiked(int size) {
        String query = """
                    SELECT f.*
                    FROM film AS f
                    LEFT JOIN "like" AS l ON f.id = l.film_id
                    GROUP BY f.id
                    ORDER BY COUNT(l.id) DESC
                    LIMIT ?;
                """;
        return jdbcTemplate.query(query, mapper, size).stream().map(this::addAllAttributesToFilm).toList();
    }

    @Override
    public List<Film> findByDirector(Integer directorId, SortOrder order) {
        String query = """
                SELECT f.* from film f
                JOIN film_director fd on f.id = fd.film_id
                WHERE fd.director_id = ?;
                """;

        switch (order) {
            case LIKES -> {
                query = """
                        SELECT f.* FROM film f
                        JOIN film_director fd ON f.id = fd.film_id
                        JOIN "like" l ON f.id = l.film_id
                        WHERE fd.director_id = ?
                        GROUP BY f.id
                        ORDER BY count(DISTINCT l.user_id) DESC;
                        """;
            }
            case YEAR -> {
                query = """
                        SELECT f.* FROM film f
                        JOIN film_director fd ON f.id = fd.film_id
                        WHERE fd.director_id = ?
                        ORDER BY f.release_date ASC;
                        """;
            }
        }
        List<Film> films = jdbcTemplate.query(query, mapper, directorId);
        films.forEach(this::addAllAttributesToFilm);
        return films;
    }

    private Set<Integer> extractGenreIdSet(Film film) {
        return film.getGenres().stream()
                .mapToInt(Genre::getId)
                .boxed()
                .collect(Collectors.toSet());
    }

    @Component
    @RequiredArgsConstructor
    private static class FilmRowMapper implements RowMapper<Film> {
        private final MpaService mpaService;
        private final GenreService genreService;

        @Override
        public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return Film.builder()
                    .id(resultSet.getInt("ID"))
                    .name(resultSet.getString("NAME"))
                    .description(resultSet.getString("DESCRIPTION"))
                    .releaseDate(resultSet.getDate("RELEASE_DATE").toLocalDate())
                    .duration(resultSet.getInt("DURATION"))
                    .mpa(mpaService.findById(resultSet.getInt("MPA_ID")))
                    .genres(genreService.findGenreByFilmId(resultSet.getInt("ID")))
                    .build();
        }
    }
}
