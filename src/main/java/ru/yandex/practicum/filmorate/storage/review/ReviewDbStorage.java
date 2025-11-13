package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ExceptionType;
import ru.yandex.practicum.filmorate.exception.LoggedException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Review> mapper = new ReviewRowMapper();

    @Override
    public Review create(Review review) {
        String query = """
                INSERT INTO review (content, is_positive, user_id, film_id, useful)
                VALUES (?, ?, ?, ?, ?);
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(query, new String[]{"id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            ps.setInt(5, review.getUseful());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() == null) {
            LoggedException.throwNew(ExceptionType.UNEXPECTED_ERROR, getClass(), List.of());
        }

        review.setReviewId(keyHolder.getKey().intValue());
        return review;
    }

    @Override
    public Review update(Review review) {
        String query = """
                UPDATE review
                SET content = ?,
                    is_positive = ?
                WHERE id = ?;
                """;
        jdbcTemplate.update(query, review.getContent(), review.getIsPositive(), review.getReviewId());
        return review;
    }

    @Override
    public Integer delete(Integer id) {
        String query = "DELETE FROM review WHERE id = ?;";
        int deleted = jdbcTemplate.update(query, id);
        if (deleted == 0) {
            LoggedException.throwNew(ExceptionType.REVIEW_NOT_FOUND, getClass(), List.of(id));
        }
        return id;
    }

    @Override
    public Review findById(Integer id) {
        String query = "SELECT * FROM review WHERE id = ?;";
        List<Review> result = jdbcTemplate.query(query, mapper, id);
        if (result.isEmpty()) {
            LoggedException.throwNew(ExceptionType.REVIEW_NOT_FOUND, getClass(), List.of(id));
        }
        return result.getFirst();
    }

    @Override
    public List<Review> findAll(Integer filmId, int count) {
        StringBuilder query = new StringBuilder("SELECT * FROM review");
        List<Object> params = new ArrayList<>();
        if (filmId != null) {
            query.append(" WHERE film_id = ?");
            params.add(filmId);
        }
        query.append(" ORDER BY useful DESC, id ASC LIMIT ?;");
        params.add(count);
        return jdbcTemplate.query(query.toString(), mapper, params.toArray());
    }

    @Override
    public void addUseful(Integer reviewId, Integer userId) {
        applyReaction(reviewId, userId, true);
    }

    @Override
    public void addUseless(Integer reviewId, Integer userId) {
        applyReaction(reviewId, userId, false);
    }

    private void applyReaction(Integer reviewId, Integer userId, boolean isUseful) {
        int delta = tryFlipReaction(reviewId, userId, isUseful);

        if (delta == 0) {
            delta = tryInsertReaction(reviewId, userId, isUseful);
        }

        if (delta != 0) {
            updateUseful(reviewId, delta);
        }
    }

    /**
     * Пытается переключить существующую реакцию пользователя.
     * Возвращает +2 / -2, если реакция была переключена, или 0, если ничего не изменилось.
     */
    private int tryFlipReaction(Integer reviewId, Integer userId, boolean isUseful) {
        int updated = jdbcTemplate.update("""
                        UPDATE review_feedback
                           SET is_useful = ?
                         WHERE review_id = ?
                           AND user_id   = ?
                           AND is_useful <> ?
                        """,
                isUseful, reviewId, userId, isUseful
        );

        if (updated == 1) {
            // дизлайк→лайк: +2; лайк→дизлайк: -2
            return isUseful ? +2 : -2;
        }

        return 0;
    }

    /**
     * Пытается вставить новую реакцию, если её ещё не было.
     * Возвращает +1 / -1, если реакция добавлена, или 0, если ничего не изменилось.
     */
    private int tryInsertReaction(Integer reviewId, Integer userId, boolean isUseful) {
        int inserted = jdbcTemplate.update("""
                        INSERT INTO review_feedback (review_id, user_id, is_useful)
                        SELECT ?, ?, ?
                         WHERE NOT EXISTS (
                               SELECT 1 FROM review_feedback
                                WHERE review_id = ?
                                  AND user_id   = ?
                         )
                        """,
                reviewId, userId, isUseful, reviewId, userId
        );

        if (inserted == 1) {
            // новая реакция: лайк +1, дизлайк -1
            return isUseful ? +1 : -1;
        }

        return 0;
    }

    /**
     * Применяет изменение к счётчику полезности отзыва.
     */
    private void updateUseful(Integer reviewId, int delta) {
        jdbcTemplate.update(
                "UPDATE review SET useful = useful + ? WHERE id = ?",
                delta, reviewId
        );
    }


    @Override
    public void removeUseful(Integer reviewId, Integer userId) {
        removeReaction(reviewId, userId, true);
    }

    @Override
    public void removeUseless(Integer reviewId, Integer userId) {
        removeReaction(reviewId, userId, false);
    }

    private void removeReaction(Integer reviewId, Integer userId, boolean isUseful) {
        int deleted = jdbcTemplate.update("""
                        DELETE FROM review_feedback
                         WHERE review_id = ?
                           AND user_id   = ?
                           AND is_useful = ?
                        """,
                reviewId, userId, isUseful
        );

        if (deleted == 1) {
            // сняли лайк → -1; сняли дизлайк → +1
            int delta = isUseful ? -1 : +1;
            updateUseful(reviewId, delta);
        }
    }

    private static class ReviewRowMapper implements RowMapper<Review> {
        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Review.builder()
                    .reviewId(rs.getInt("id"))
                    .content(rs.getString("content"))
                    .isPositive(rs.getBoolean("is_positive"))
                    .userId(rs.getInt("user_id"))
                    .filmId(rs.getInt("film_id"))
                    .useful(rs.getInt("useful"))
                    .build();
        }
    }
}
