package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Primary
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FeedRowMapper mapper = new FeedRowMapper();

    @Override
    public List<Feed> findAll() {
        String query = "SELECT * FROM feed";
        return jdbcTemplate.query(query, mapper);
    }

    @Override
    public List<Feed> findById(Integer id) {
        String query = "SELECT * FROM feed WHERE user_id = ?";
        return jdbcTemplate.query(query, mapper, id);
    }

    @Override
    public Integer getLikeId(Integer filmId, Integer userId) {
        String query = """
                SELECT id FROM "like" WHERE film_id = ? AND user_id = ?;
                """;
        try {
            return jdbcTemplate.queryForObject(query, Integer.class, filmId, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Integer getFriendId(Integer userIdA, Integer userIdB) {
        String query = """
                SELECT id FROM friends WHERE request_from_id = ? AND request_to_id = ?
                """;
        try {
            return jdbcTemplate.queryForObject(query, Integer.class, userIdA, userIdB);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void save(Feed feed) {
        String query = "INSERT INTO feed (date, user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?);";
        Date date = new Date(feed.getTimestamp());
        jdbcTemplate.update(query,
                date,
                feed.getUserId(),
                feed.getEventType(),
                feed.getOperation(),
                feed.getEntityId());
    }

    @Component
    @RequiredArgsConstructor
    private static class FeedRowMapper implements RowMapper<Feed> {
        @Override
        public Feed mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            Feed feed = new Feed();
            feed.setEventId(resultSet.getInt("ID"));
            feed.setTimestamp(resultSet.getTimestamp("DATE").toInstant().toEpochMilli());
            feed.setUserId(resultSet.getInt("USER_ID"));
            feed.setEventType(resultSet.getString("EVENT_TYPE"));
            feed.setOperation(resultSet.getString("OPERATION"));
            feed.setEntityId(resultSet.getInt("ENTITY_ID"));
            return feed;
        }
    }
}