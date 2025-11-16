package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.FeedEventType;
import ru.yandex.practicum.filmorate.model.OperationType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Primary
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FeedRowMapper mapper;

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
    public void save(Integer userId, FeedEventType feedEventType, OperationType operationType,
                     Integer entityId) {
        String query = "INSERT INTO feed (date, user_id, event_type, operation_type, entity_id) VALUES (?, ?, ?, ?, ?);";
        jdbcTemplate.update(query,
                Instant.now(),
                userId,
                feedEventType.toString(),
                operationType.toString(),
                entityId);
    }

    @Component
    @RequiredArgsConstructor
    private static class FeedRowMapper implements RowMapper<Feed> {
        @Override
        public Feed mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            Feed feed = new Feed();
            feed.setEventId(resultSet.getInt("ID"));
            feed.setTimestamp(resultSet.getTimestamp("DATE").getTime());
            feed.setUserId(resultSet.getInt("USER_ID"));
            feed.setEventType(FeedEventType.valueOf(resultSet.getString("EVENT_TYPE")));
            feed.setOperation(OperationType.valueOf(resultSet.getString("OPERATION_TYPE")));
            feed.setEntityId(resultSet.getInt("ENTITY_ID"));
            return feed;
        }
    }
}