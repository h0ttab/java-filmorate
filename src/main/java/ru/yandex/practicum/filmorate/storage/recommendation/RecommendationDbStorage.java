package ru.yandex.practicum.filmorate.storage.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmRowMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Реализация хранилища рекомендаций фильмов с использованием базы данных.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationDbStorage implements RecommendationStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    /**
     * Получает список идентификаторов фильмов, которым поставил лайк указанный пользователь.
     *
     * @param userId идентификатор пользователя
     * @return список идентификаторов фильмов, которым поставил лайк пользователь
     */
    @Override
    public List<Integer> getLikedFilmsByUserId(Integer userId) {
        String query = """
                SELECT film_id FROM "like"
                WHERE user_id = ?;
                """;
        return jdbcTemplate.queryForList(query, Integer.class, userId);
    }

    /**
     * Получает матрицу лайков пользователей по фильмам.
     * Ключ внешней карты - идентификатор пользователя.
     * Ключ внутренней карты - идентификатор фильма.
     * Значение внутренней карты - рейтинг (1.0 для лайка, 0.0 для отсутствия лайка).
     *
     * @return матрица лайков пользователей по фильмам
     */
    @Override
    public Map<Integer, Map<Integer, Double>> getUserFilmLikesMatrix() {
        // Получаем всех пользователей, которые поставили хотя бы один лайк
        String userQuery = """
                SELECT DISTINCT user_id FROM "like";
                """;
        List<Integer> userIds = jdbcTemplate.queryForList(userQuery, Integer.class);

        // Получаем все фильмы, которым поставили хотя бы один лайк
        String filmQuery = """
                SELECT DISTINCT film_id FROM "like";
                """;
        List<Integer> filmIds = jdbcTemplate.queryForList(filmQuery, Integer.class);

        // Получаем все лайки
        String likesQuery = """
                SELECT user_id, film_id FROM "like";
                """;
        List<Map<String, Object>> likes = jdbcTemplate.queryForList(likesQuery);

        // Формируем матрицу лайков
        Map<Integer, Map<Integer, Double>> matrix = new HashMap<>();

        // Инициализируем матрицу для всех пользователей
        for (Integer userId : userIds) {
            Map<Integer, Double> userLikes = new HashMap<>();
            matrix.put(userId, userLikes);
        }

        // Заполняем матрицу лайками (1.0 для лайка)
        for (Map<String, Object> like : likes) {
            Integer userId = (Integer) like.get("user_id");
            Integer filmId = (Integer) like.get("film_id");
            matrix.get(userId).put(filmId, 1.0);
        }

        log.info("Сформирована матрица лайков для {} пользователей и {} фильмов", userIds.size(), filmIds.size());
        return matrix;
    }

    /**
     * Получает список рекомендованных фильмов для указанного пользователя на основе алгоритма Slope One.
     * Метод использует SQL-запрос для получения рекомендаций напрямую из базы данных.
     *
     * @param userId идентификатор пользователя
     * @return список рекомендованных фильмов
     */
    @Override
    public List<Film> getRecommendations(Integer userId) {
        // Получаем список фильмов, которым поставил лайк пользователь
        List<Integer> userLikedFilms = getLikedFilmsByUserId(userId);

        if (userLikedFilms.isEmpty()) {
            log.info("Пользователь с id {} не поставил ни одного лайка, рекомендации не могут быть сформированы", userId);
            return List.of();
        }

        // Формируем строку с идентификаторами фильмов, которым поставил лайк пользователь
        String userLikedFilmsStr = userLikedFilms.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        // SQL-запрос для получения рекомендаций
        // Находим пользователей с максимальным пересечением по лайкам
        // и рекомендуем фильмы, которые они лайкнули, а текущий пользователь - нет
        String query = """
                WITH user_likes AS (
                    SELECT film_id
                    FROM "like"
                    WHERE user_id = ?
                ),
                similar_users AS (
                    SELECT l.user_id, COUNT(*) as common_likes
                    FROM "like" l
                    JOIN user_likes ul ON l.film_id = ul.film_id
                    WHERE l.user_id != ?
                    GROUP BY l.user_id
                    ORDER BY common_likes DESC
                ),
                recommendations AS (
                    SELECT DISTINCT l.film_id, su.common_likes
                    FROM "like" l
                    JOIN similar_users su ON l.user_id = su.user_id
                    WHERE l.film_id NOT IN (
                        SELECT film_id FROM user_likes
                    )
                    ORDER BY su.common_likes DESC
                )
                SELECT f.*
                FROM film f
                JOIN recommendations r ON f.id = r.film_id
                ORDER BY f.id
                LIMIT 10;
                """;

        List<Film> recommendations = jdbcTemplate.query(query, filmRowMapper, userId, userId);
        log.info("Сформированы рекомендации для пользователя с id {}: {} фильмов", userId, recommendations.size());
        return recommendations;
    }
}