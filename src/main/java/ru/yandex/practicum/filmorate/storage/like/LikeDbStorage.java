package ru.yandex.practicum.filmorate.storage.like;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeDbStorage implements LikeStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addLike(Integer filmId, Integer userId) {
        String query = """
                INSERT INTO "like" (film_id, user_id)
                VALUES (?, ?);
                """;
        jdbcTemplate.update(query, filmId, userId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        String query = """
                DELETE FROM "like"
                WHERE film_id = ?
                AND user_id = ?;
                """;
        jdbcTemplate.update(query, filmId, userId);
    }

    @Override
    public List<Integer> getLikesByFilmId(Integer filmId) {
        String query = """
                SELECT user_id FROM "like"
                WHERE film_id = ?;
                """;
        return jdbcTemplate.queryForList(query, Integer.class, filmId);
    }
}