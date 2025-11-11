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
    public List<Feed> findAll() {
        String query = "SELECT * FROM feed ORDER BY date DESC;";
        return jdbcTemplate.query(query, mapper);
    }

    @Component
    @RequiredArgsConstructor
    private static class FeedRowMapper implements RowMapper<Feed> {
        @Override
        public Feed mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return Feed.builder()
                    .eventId(resultSet.getInt("ID"))
                    .timestamp(resultSet.getTimestamp("TIMESTAMP").toLocalDateTime().toLocalDate())
                    .userId(resultSet.getInt("USER_ID"))
                    .eventType(resultSet.getString("EVENT_TYPE"))
                    .operation(resultSet.getString("OPERATION"))
                    .entityId(resultSet.getInt("ENTITY_ID"))
                    .build();
        }
    }
}