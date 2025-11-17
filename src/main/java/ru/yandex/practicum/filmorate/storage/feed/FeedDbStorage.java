package ru.yandex.practicum.filmorate.storage.feed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.*;

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
            Feed feed = Feed.builder()
                    .timestamp(resultSet.getTimestamp("DATE").getTime())
                    .userId(resultSet.getInt("USER_ID"))
                    .eventType(FeedEventType.valueOf(resultSet.getString("EVENT_TYPE")))
                    .operation(OperationType.valueOf(resultSet.getString("OPERATION_TYPE")))
                    .eventId(resultSet.getInt("ID"))
                    .entityId(resultSet.getInt("ENTITY_ID"))
                    .build();
            return feed;
        }
    }
}