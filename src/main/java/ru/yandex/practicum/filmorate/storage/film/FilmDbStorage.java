package ru.yandex.practicum.filmorate.storage.film;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ExceptionType;
import ru.yandex.practicum.filmorate.exception.LoggedException;
import ru.yandex.practicum.filmorate.model.Film;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper mapper;

    @Override
    public List<Film> findAll() {
        String query = "SELECT * FROM film;";
        return jdbcTemplate.query(query, mapper);
    }

    @Override
    public Film findById(Integer filmId) {
        String query = "SELECT * FROM film WHERE id = ?;";
        List<Film> result = jdbcTemplate.query(query, mapper, filmId);
        if (result.isEmpty()) {
            LoggedException.throwNew(ExceptionType.FILM_NOT_FOUND, getClass(), List.of(filmId));
        }
        return result.getFirst();
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
        return film;
    }

    @Override
    public Film update(Film film) {

        String queryFilmUpdateWithMpa = """
                    UPDATE film f
                    SET name = ?,
                        description = ?,
                        release_date = ?,
                        duration = ?,
                        mpa_id = ?
                    WHERE f.id = ?;
                """;
        String queryFilmUpdateNoMpa = """
                UPDATE film f
                SET name = ?,
                    description = ?,
                    release_date = ?,
                    duration = ?
                WHERE f.id = ?;
                """;
        int updatedFilmRows;

        if (film.getMpa() == null) {
            updatedFilmRows = jdbcTemplate.update(
                    queryFilmUpdateNoMpa,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getId()
            );
        } else {
            updatedFilmRows = jdbcTemplate.update(
                    queryFilmUpdateWithMpa,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    film.getId()
            );
        }
        if (updatedFilmRows == 0) {
            LoggedException.throwNew(ExceptionType.FILM_NOT_FOUND, getClass(), List.of(film.getId()));
        }
        log.info("Обновлён фильм id {}. Новое значение: {}", film.getId(), film);
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
        return jdbcTemplate.query(query, mapper, size);
    }

    /**
     * Находит топ N фильмов по количеству лайков с возможностью фильтрации по жанру и году выпуска.
     * Метод формирует динамический SQL-запрос в зависимости от переданных параметров фильтрации.
     *
     * @param count   количество фильмов для вывода
     * @param genreId идентификатор жанра для фильтрации (null для выборки всех жанров)
     * @param year    год выпуска фильма для фильтрации (null для выборки всех лет)
     * @return отсортированный по убыванию список фильмов с наибольшим количеством лайков,
     * соответствующий указанным критериям фильтрации
     * @see Film
     */
    @Override
    public List<Film> findTopLiked(int count, Integer genreId, Integer year) {
        StringBuilder queryBuilder = new StringBuilder("""
                SELECT f.*, COUNT(l.id) as like_count
                FROM film AS f
                LEFT JOIN "like" AS l ON f.id = l.film_id
                LEFT JOIN film_genre AS fg ON f.id = fg.film_id
                """);

        // Начинаем формировать условия WHERE
        List<Object> params = new ArrayList<>();
        boolean hasConditions = false;

        // Добавляем условие по жанру, если указан genreId
        if (genreId != null) {
            queryBuilder.append("WHERE fg.genre_id = ? ");
            params.add(genreId);
            hasConditions = true;
        }

        // Добавляем условие по году, если указан year
        if (year != null) {
            if (hasConditions) {
                queryBuilder.append("AND ");
            } else {
                queryBuilder.append("WHERE ");
            }
            queryBuilder.append("EXTRACT(YEAR FROM f.release_date) = ? ");
            params.add(year);
        }

        // Добавляем группировку, сортировку и лимит
        queryBuilder.append("""
                GROUP BY f.id
                ORDER BY like_count DESC
                LIMIT ?
                """);

        // Добавляем параметр limit
        params.add(count);

        // Выполняем запрос с параметрами
        return jdbcTemplate.query(queryBuilder.toString(), mapper, params.toArray());
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
        return jdbcTemplate.query(query, mapper, directorId);
    }

    @Override
    public List<Film> findCommonFilms(Integer userId, Integer friendId) {
        String query = """
                SELECT f.*
                FROM film f
                JOIN "like" l_user ON f.id = l_user.film_id AND l_user.user_id = ?
                JOIN "like" l_friend ON f.id = l_friend.film_id AND l_friend.user_id = ?
                LEFT JOIN "like" l_all ON f.id = l_all.film_id
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                ORDER BY COUNT(l_all.user_id) DESC, f.id;
                """;

        return jdbcTemplate.query(query, mapper, userId, friendId);
    }

    @Component
    @RequiredArgsConstructor
    public static class FilmRowMapper implements RowMapper<Film> {
        @Override
        public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return Film.builder()
                    .id(resultSet.getInt("ID"))
                    .name(resultSet.getString("NAME"))
                    .description(resultSet.getString("DESCRIPTION"))
                    .releaseDate(resultSet.getDate("RELEASE_DATE").toLocalDate())
                    .duration(resultSet.getInt("DURATION"))
                    .build();
        }
    }
}
