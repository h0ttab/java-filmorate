package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Primary
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FeedRowMapper mapper = new FeedRowMapper();

    @Override
    public List<Feed> findAll(Integer id) {
        String query = "SELECT * FROM feed WHERE user_id = ? ORDER BY date DESC;";
        return jdbcTemplate.query(query, mapper, id);
    }

    @Override
    public void save(Feed feed) {
        String query = "INSERT INTO feed (date, user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?);";
        jdbcTemplate.update(query,
                feed.getTimestamp(),
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
            feed.setTimestamp(resultSet.getTimestamp("DATE").toLocalDateTime().toLocalDate());
            feed.setUserId(resultSet.getInt("USER_ID"));
            feed.setEventType(resultSet.getString("EVENT_TYPE"));
            feed.setOperation(resultSet.getString("OPERATION"));
            feed.setEntityId(resultSet.getInt("ENTITY_ID"));
            return feed;
        }
    }
}