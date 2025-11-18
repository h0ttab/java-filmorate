package ru.yandex.practicum.filmorate.storage.recommendation;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

/**
 * Интерфейс для хранилища рекомендаций фильмов.
 * Предоставляет методы для работы с рекомендациями фильмов.
 */
public interface RecommendationStorage {
    
    /**
     * Получает список фильмов, которым поставил лайк указанный пользователь.
     *
     * @param userId идентификатор пользователя
     * @return список идентификаторов фильмов, которым поставил лайк пользователь
     */
    List<Integer> getLikedFilmsByUserId(Integer userId);
    
    /**
     * Получает матрицу лайков пользователей по фильмам.
     * Ключ внешней карты - идентификатор пользователя.
     * Ключ внутренней карты - идентификатор фильма.
     * Значение внутренней карты - рейтинг (1.0 для лайка, 0.0 для отсутствия лайка).
     *
     * @return матрица лайков пользователей по фильмам
     */
    Map<Integer, Map<Integer, Double>> getUserFilmLikesMatrix();
    
    /**
     * Получает список рекомендованных фильмов для указанного пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список рекомендованных фильмов
     */
    List<Film> getRecommendations(Integer userId);
}