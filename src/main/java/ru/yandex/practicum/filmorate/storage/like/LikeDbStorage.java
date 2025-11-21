package ru.yandex.practicum.filmorate.storage.like;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeDbStorage implements LikeStorage {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final LikeBatchRowMapper likeBatchRowMapper;

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

    @Override
    public List<LikeBatchDto> getLikesByFilmIdList(List<Integer> filmIdList) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("filmIds", filmIdList);
        String query = """
                SELECT
                	film_id,
                	GROUP_CONCAT(user_id SEPARATOR ',') AS user_id_list
                FROM "like"
                WHERE film_id in (:filmIds)
                GROUP BY film_id
                ORDER BY film_id;
                """;
        return namedParameterJdbcTemplate.query(query, parameterSource, likeBatchRowMapper);
    }

    @Builder
    public record LikeBatchDto(Integer filmId, String likeList) {
    }

    @Component
    private static class LikeBatchRowMapper implements RowMapper<LikeBatchDto> {
        @Override
        public LikeBatchDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return LikeBatchDto.builder()
                    .filmId(rs.getInt("film_id"))
                    .likeList(rs.getString("user_id_list"))
                    .build();
        }
    }
}