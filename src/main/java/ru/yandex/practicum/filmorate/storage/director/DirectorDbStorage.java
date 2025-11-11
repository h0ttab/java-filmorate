package ru.yandex.practicum.filmorate.storage.director;

import java.sql.*;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ExceptionType;
import ru.yandex.practicum.filmorate.exception.LoggedException;
import ru.yandex.practicum.filmorate.model.Director;

@Primary
@Component
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;
    private final DirectorRowMapper mapper;

    @Override
    public Director create(Director director) {
        String query = """
                INSERT INTO director (name)
                VALUES (?);
                """;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(query, new String[]{"id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        if (Optional.ofNullable(keyHolder.getKey()).isEmpty()) {
            LoggedException.throwNew(ExceptionType.UNEXPECTED_ERROR, getClass(), List.of());
        }

        return director.toBuilder().id(keyHolder.getKey().intValue()).build();
    }

    @Override
    public List<Director> findAll() {
        String query = """
                SELECT * FROM director;
                """;
        return jdbcTemplate.query(query, mapper);
    }

    @Override
    public Director findById(Integer directorId) {
        String query = """
                SELECT * FROM director
                WHERE id = ?;
                """;
        return jdbcTemplate.queryForObject(query, mapper, directorId);
    }

    @Override
    public List<Director> findByFilm(Integer filmId) {
        String query = """
                SELECT d.* FROM director d
                JOIN film_director fd ON d.id = fd.director_id
                WHERE fd.film_id = ?;
                """;
        return jdbcTemplate.query(query, mapper, filmId);
    }

    @Override
    public Director update(Director director) {
        String query = """
                UPDATE director
                SET name = ?
                WHERE id = ?;
                """;
        int updatedRows = jdbcTemplate.update(query, director.getName(), director.getId());
        if (updatedRows == 0) {
            LoggedException.throwNew(ExceptionType.DIRECTOR_NOT_FOUND, getClass(), List.of(director.getId()));
        }
        return director;
    }

    @Override
    public void delete(Integer directorId) {
        String query = """
                DELETE FROM director
                WHERE id = ?;
                """;
        int deletedRows = jdbcTemplate.update(query, directorId);
        if (deletedRows == 0) {
            LoggedException.throwNew(ExceptionType.DIRECTOR_NOT_FOUND, getClass(), List.of(directorId));
        }
    }

    @Override
    public void linkDirectorsToFilm(Integer filmId, Set<Integer> directorIds, boolean clearExisting) {
        StringBuilder insertQuery = new StringBuilder();

        if (clearExisting) {
            String deleteDirectorsOfFilmQuery = "DELETE FROM film_director WHERE film_id = ?;";
            jdbcTemplate.update(deleteDirectorsOfFilmQuery, filmId);
        }

        for (Integer directorId : directorIds) {
            insertQuery.append(String.format("INSERT INTO film_director (film_id, director_id) VALUES (%d, %d);",
                    filmId, directorId));
            insertQuery.append("\n");
        }

        jdbcTemplate.update(insertQuery.toString());
    }

    @Component
    private static class DirectorRowMapper implements RowMapper<Director> {
        @Override
        public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Director.builder().id(rs.getInt("id")).name(rs.getString("name")).build();
        }
    }
}