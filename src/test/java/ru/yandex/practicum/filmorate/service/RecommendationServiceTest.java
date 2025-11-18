package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.recommendation.RecommendationStorage;
import ru.yandex.practicum.filmorate.util.Validators;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Тесты для сервиса рекомендаций фильмов.
 */
@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

    @Mock
    private RecommendationStorage recommendationStorage;

    @Mock
    private Validators validators;

    @InjectMocks
    private RecommendationService recommendationService;

    private Film film1;
    private Film film2;
    private final Integer userId = 1;

    @BeforeEach
    void setUp() {
        // Создаем тестовые фильмы
        film1 = Film.builder()
                .id(1)
                .name("Фильм 1")
                .description("Описание фильма 1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).name("G").build())
                .build();

        film2 = Film.builder()
                .id(2)
                .name("Фильм 2")
                .description("Описание фильма 2")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(130)
                .mpa(Mpa.builder().id(2).name("PG").build())
                .build();
    }

    /**
     * Тест метода getRecommendations.
     * Проверяет, что метод возвращает список рекомендованных фильмов,
     * полученных из хранилища рекомендаций.
     */
    @Test
    void getRecommendationsTest() {
        // Подготавливаем данные
        List<Film> expectedRecommendations = List.of(film1, film2);

        // Настраиваем поведение моков
        doNothing().when(validators).validateUserExits(userId, RecommendationService.class);
        when(recommendationStorage.getRecommendations(userId)).thenReturn(expectedRecommendations);

        // Вызываем тестируемый метод
        List<Film> actualRecommendations = recommendationService.getRecommendations(userId);

        // Проверяем результат
        assertEquals(expectedRecommendations.size(), actualRecommendations.size(), "Размер списка рекомендаций должен совпадать");
        assertEquals(expectedRecommendations, actualRecommendations, "Списки рекомендаций должны совпадать");

        // Проверяем, что методы моков были вызваны с правильными параметрами
        verify(validators).validateUserExits(userId, RecommendationService.class);
        verify(recommendationStorage).getRecommendations(userId);
    }

    /**
     * Тест метода getRecommendations при пустом списке рекомендаций.
     * Проверяет, что метод корректно обрабатывает случай, когда нет рекомендаций для пользователя.
     */
    @Test
    void getRecommendationsEmptyListTest() {
        // Подготавливаем данные
        List<Film> expectedRecommendations = List.of();

        // Настраиваем поведение моков
        doNothing().when(validators).validateUserExits(userId, RecommendationService.class);
        when(recommendationStorage.getRecommendations(userId)).thenReturn(expectedRecommendations);

        // Вызываем тестируемый метод
        List<Film> actualRecommendations = recommendationService.getRecommendations(userId);

        // Проверяем результат
        assertEquals(0, actualRecommendations.size(), "Размер списка рекомендаций должен быть 0");

        // Проверяем, что методы моков были вызваны с правильными параметрами
        verify(validators).validateUserExits(userId, RecommendationService.class);
        verify(recommendationStorage).getRecommendations(userId);
    }
}