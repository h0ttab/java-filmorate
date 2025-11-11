package ru.yandex.practicum.filmorate.storage.genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Genre> mapper = new GenreRowMapper();

    @Override
    public List<Genre> findAll() {
        String query = """
                SELECT * FROM genre;
                """;
        return jdbcTemplate.query(query, mapper);
    }

    @Override
    public Genre findById(Integer genreId) {
        String query = """
                SELECT * FROM genre
                WHERE id = ?;
                """;
        return jdbcTemplate.queryForObject(query, mapper, genreId);
    }

    @Override
    public List<Genre> findGenreByFilmId(Integer filmId) {
        String query = """
                    SELECT g.*
                    FROM genre g
                    JOIN film_genre fg ON g.id = fg.genre_id
                    WHERE fg.film_id = ?
                    ORDER BY fg.genre_id;
                """;
        return jdbcTemplate.query(query, mapper, filmId);
    }


    @Override
    public void linkGenresToFilm(Integer filmId, Set<Integer> genreIdSet, boolean clearExisting) {
        StringBuilder insertQuery = new StringBuilder();

        for (Integer genreId : genreIdSet) {
            insertQuery.append(String.format("INSERT INTO film_genre (film_id, genre_id) VALUES (%d, %d);", filmId, genreId));
            insertQuery.append("\n");
        }

        if (clearExisting) {
            String deleteGenresOfFilmQuery = "DELETE FROM film_genre WHERE film_id = ?;";
            jdbcTemplate.update(deleteGenresOfFilmQuery, filmId);
        }
        jdbcTemplate.update(insertQuery.toString());
    }

    private static class GenreRowMapper implements RowMapper<Genre> {
        @Override
        public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Genre.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .build();
        }
    }
}