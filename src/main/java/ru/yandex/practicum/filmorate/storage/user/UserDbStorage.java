package ru.yandex.practicum.filmorate.storage.user;

import java.sql.Date;
import java.sql.*;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ExceptionType;
import ru.yandex.practicum.filmorate.exception.LoggedException;
import ru.yandex.practicum.filmorate.model.User;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorage implements UserStorage {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> mapper = new UserRowMapper();

    @Override
    public List<User> findAll() {
        String query = "SELECT * FROM \"user\";";
        return jdbcTemplate.query(query, mapper);
    }

    @Override
    public User findById(Integer userId) {
        String query = "SELECT * FROM \"user\" WHERE id = ?;";
        List<User> result = jdbcTemplate.query(query, mapper, userId);
        if (result.isEmpty()) {
            LoggedException.throwNew(ExceptionType.USER_NOT_FOUND, getClass(), List.of(userId));
        }
        return result.getFirst();
    }

    @Override
    public User create(User user) {
        String query = """
                INSERT INTO "user" (EMAIL, LOGIN, NAME, BIRTHDAY)
                VALUES (?, ?, ?, ?);
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(query, new String[]{"id"});
                    ps.setString(1, user.getEmail());
                    ps.setString(2, user.getLogin());
                    ps.setString(3, user.getName());
                    ps.setDate(4, Date.valueOf(user.getBirthday()));
                    return ps;
                }, keyHolder);

        if (Optional.ofNullable(keyHolder.getKey()).isPresent()) {
            user.setId(keyHolder.getKey().intValue());
            log.info("Добавлен новый пользователь: {}", user);
            return user;
        }

        throw new RuntimeException("Непредвиденная ошибка при добавлении пользователя.");
    }

    @Override
    public User update(User user) {
        String query = """
                    UPDATE "user"
                    SET email = ?, login = ?, name = ?, birthday = ?
                    WHERE "user".id = ?;
                """;

        int updatedRows = jdbcTemplate.update(
                query,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        if (updatedRows == 0) {
            LoggedException.throwNew(ExceptionType.USER_NOT_FOUND, getClass(), List.of(user.getId()));
        }
        log.info("Обновлён пользователь id {}. Новое значение: {}", user.getId(), user);
        return user;
    }

    @Override
    public Integer delete(Integer userId) {
        String query = "DELETE FROM \"user\" WHERE id = ?";
        int deletedRows = jdbcTemplate.update(query, userId);
        if (deletedRows == 0) {
            LoggedException.throwNew(ExceptionType.USER_NOT_FOUND, getClass(), List.of(userId));
        }
        log.info("Удалён пользователь id {}", userId);
        return userId;
    }

    @Override
    public List<User> getFriends(Integer userId) {
        findById(userId);
        String query = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM friends f
                JOIN "user" u ON f.request_to_id = u.id
                WHERE f.request_from_id = ?
                """;
        List<User> response = jdbcTemplate.query(query, mapper, userId);
        if (response.isEmpty()) {
            response = new ArrayList<>();
        }
        return response;
    }

    @Override
    public List<User> getCommonFriends(Integer userIdA, Integer userIdB) {
        String query = """
                SELECT u.* FROM "user" u
                JOIN friends a
                  ON a.request_to_id = u.id
                JOIN friends b
                  ON a.request_to_id = b.request_to_id
                WHERE a.request_from_id = ?
                  AND b.request_from_id = ?;
                """;
        return jdbcTemplate.query(query, mapper, userIdA, userIdB);
    }

    @Override
    public void addFriend(Integer userIdA, Integer userIdB) {
        String query = """
                INSERT INTO FRIENDS (REQUEST_FROM_ID, REQUEST_TO_ID)
                values(?, ?);
                """;
        jdbcTemplate.update(query, userIdA, userIdB);
    }

    @Override
    public void removeFriend(Integer userIdA, Integer userIdB) {
        String query = """
                DELETE FROM friends
                WHERE request_from_id = ?
                AND request_to_id = ?;
                """;
        jdbcTemplate.update(query, userIdA, userIdB);
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return User.builder()
                    .id(resultSet.getInt("ID"))
                    .email(resultSet.getString("EMAIL"))
                    .login(resultSet.getString("LOGIN"))
                    .name(resultSet.getString("NAME"))
                    .birthday(resultSet.getDate("BIRTHDAY").toLocalDate())
                    .build();
        }
    }
}