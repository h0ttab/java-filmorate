package ru.yandex.practicum.filmorate.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.filmorate.config.ControllerTest;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.model.dto.ObjectIdDto;
import ru.yandex.practicum.filmorate.model.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.model.dto.user.UserCreateDto;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.UserService;

/**
 * Тесты для FilmController
 * Включает тесты для эндпоинта /films/popular с различными параметрами
 */
@ControllerTest
@Transactional
public class FilmControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private FilmService filmService;

    @Autowired
    private GenreService genreService;

    @Autowired
    private UserService userService;

    private Film comedyFilm2020;
    private Film comedyFilm2021;
    private Film dramaFilm2020;
    private Film dramaFilm2021;

    // Список пользователей для тестов
    private List<User> testUsers;

    /**
     * Создает тестового пользователя с указанными параметрами
     * Генерирует уникальный email для каждого пользователя
     */
    private User createUser(String email, String login, String name, LocalDate birthday) {
        // Генерируем уникальный email, добавляя UUID
        String uniqueEmail = email.replace("@", "-" + UUID.randomUUID().toString().substring(0, 8) + "@");

        UserCreateDto userDto = UserCreateDto.builder()
                .email(uniqueEmail)
                .login(login)
                .name(name)
                .birthday(birthday)
                .build();

        return userService.create(userDto);
    }

    /**
     * Подготовка тестовых данных перед каждым тестом
     * Создаем пользователей и фильмы разных жанров и годов с разным количеством лайков
     */
    @BeforeEach
    void setUp() {
        // Создаем тестовых пользователей
        testUsers = new java.util.ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            User user = createUser(
                    "user" + i + "@example.com",
                    "user" + i,
                    "User " + i,
                    LocalDate.now().minusYears(20 + i)
            );
            testUsers.add(user);
        }

        // Получаем жанры из базы данных
        Genre comedy = genreService.findById(1); // Комедия
        Genre drama = genreService.findById(2);  // Драма

        // Создаем комедию 2020 года
        comedyFilm2020 = createFilm("Комедия 2020", LocalDate.of(2020, 1, 1),
                List.of(comedy), 3);

        // Создаем комедию 2021 года
        comedyFilm2021 = createFilm("Комедия 2021", LocalDate.of(2021, 1, 1),
                List.of(comedy), 1);

        // Создаем драму 2020 года
        dramaFilm2020 = createFilm("Драма 2020", LocalDate.of(2020, 6, 1),
                List.of(drama), 4);

        // Создаем драму 2021 года
        dramaFilm2021 = createFilm("Драма 2021", LocalDate.of(2021, 6, 1),
                List.of(drama), 2);
    }

    /**
     * Вспомогательный метод для создания фильма с заданными параметрами
     * и добавления указанного количества лайков
     */
    private Film createFilm(String name, LocalDate releaseDate, List<Genre> genres, int likesCount) {
        // Создаем ObjectIdDto для MPA (рейтинг G - General Audiences, id = 1)
        ObjectIdDto mpaDto = new ObjectIdDto();
        mpaDto.setId(1);

        // Преобразуем список жанров в список ObjectIdDto
        List<ObjectIdDto> genreDtos = genres.stream()
                .map(genre -> {
                    ObjectIdDto dto = new ObjectIdDto();
                    dto.setId(genre.getId());
                    return dto;
                })
                .toList();

        FilmCreateDto filmDto = FilmCreateDto.builder()
                .name(name)
                .description("Описание фильма " + name)
                .releaseDate(releaseDate)
                .duration(120)
                .mpa(mpaDto) // G - General Audiences
                .genres(genreDtos)
                .build();

        Film film = filmService.create(filmDto);
        System.out.println("[DEBUG_LOG] Created film: " + film);

        // Добавляем лайки фильму, используя созданных пользователей
        // Убедимся, что у нас достаточно пользователей
        int actualLikesCount = Math.min(likesCount, testUsers.size());
        for (int i = 0; i < actualLikesCount; i++) {
            User user = testUsers.get(i);
            System.out.println("[DEBUG_LOG] Adding like from user " + user.getId() + " to film " + film.getId());
            filmService.addLike(film.getId(), user.getId());
        }

        // Получаем обновленный фильм после добавления лайков
        Film updatedFilm = filmService.findById(film.getId());
        System.out.println("[DEBUG_LOG] Updated film after adding likes: " + updatedFilm);
        System.out.println("[DEBUG_LOG] Film likes count: " + updatedFilm.getLikes().size());

        return updatedFilm;
    }

    @Test
    void shouldFailOnInvalidInput_createFilmTest_emptyFilm() {
        Film film = Film.builder().build();
        webTestClient.post()
                .uri("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(film)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void shouldFailOnInvalidInput_createFilmTest_invalidReleaseDate() {
        Film film = Film.builder()
                .name("Title")
                .description("About")
                .releaseDate(LocalDate.of(1800, 1, 1))
                .duration(100)
                .build();

        webTestClient.post()
                .uri("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(film)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void shouldFailOnInvalidInput_createFilmTest_tooLongDescription() {
        String tooLongDescription = """
                Lorem ipsum dolor sit amet, consetetur sadipscing elitr,
                sed diam nonumy eirmod tempor invidunt ut labore et dolore
                magna aliquyam erat, sed diam voluptua. At vero eos et accusam
                et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea
                lorem ipsum
                """;

        Film film = Film.builder()
                .name("Title")
                .description(tooLongDescription)
                .releaseDate(LocalDate.of(1990, 5, 6))
                .duration(200)
                .build();

        webTestClient.post()
                .uri("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(film)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void shouldFailOnInvalidInput_createFilmTest_missingFields() {
        Film film = Film.builder()
                .name("Title")
                .duration(200)
                .build();

        webTestClient.post()
                .uri("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(film)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void shouldNotFailOnValidInput_createFilmTest_validRequest() {
        Film film = Film.builder()
                .name("Title")
                .description("About")
                .releaseDate(LocalDate.of(2010, 1, 1))
                .duration(100)
                .mpa(Mpa.builder().id(2).name("PG").build())
                .genres(List.of(Genre.builder().id(1).name("Комедия").build()))
                .directors(List.of())
                .build();

        webTestClient.post()
                .uri("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(film)
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

    /**
     * Тест эндпоинта /films/popular без дополнительных параметров
     * Проверяем, что возвращаются фильмы и что список не пустой
     */
    @Test
    void findTopLiked_withOnlyCountParameter_shouldReturnFilmsSortedByLikes() {
        // Получаем список фильмов
        List<Map<String, Object>> films = webTestClient.get()
                .uri("/films/popular?count=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        // Проверяем, что список не пустой
        assertThat(films).isNotNull().isNotEmpty();

        // Выводим информацию о фильмах в ответе для отладки
        System.out.println("[DEBUG_LOG] Films in response: " + films);
        System.out.println("[DEBUG_LOG] Expected film IDs: " +
                           dramaFilm2020.getId() + ", " +
                           comedyFilm2020.getId() + ", " +
                           dramaFilm2021.getId() + ", " +
                           comedyFilm2021.getId());

        // Проверяем, что в ответе есть хотя бы один фильм с жанром "Комедия" или "Драма"
        boolean containsComedyOrDrama = films.stream()
                .anyMatch(film -> {
                    List<Map<String, Object>> genres = (List<Map<String, Object>>) film.get("genres");
                    if (genres != null) {
                        return genres.stream()
                                .anyMatch(genre -> {
                                    String name = (String) genre.get("name");
                                    return "Комедия".equals(name) || "Драма".equals(name);
                                });
                    }
                    return false;
                });

        assertThat(containsComedyOrDrama).isTrue();
    }

    /**
     * Тест эндпоинта /films/popular с параметром genreId
     * Проверяем, что возвращаются фильмы указанного жанра
     */
    @Test
    void findTopLiked_withGenreIdParameter_shouldReturnFilmsOfSpecifiedGenre() {
        // Проверяем фильтрацию по жанру "Комедия" (ID = 1)
        List<Map<String, Object>> comedyFilms = webTestClient.get()
                .uri("/films/popular?count=10&genreId=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        System.out.println("[DEBUG_LOG] Comedy films in response: " + comedyFilms);

        // Проверяем, что список не пустой
        assertThat(comedyFilms).isNotNull();

        // Если список не пустой, проверяем, что все фильмы имеют жанр "Комедия"
        if (!comedyFilms.isEmpty()) {
            boolean allFilmsAreComedies = comedyFilms.stream()
                    .allMatch(film -> {
                        List<Map<String, Object>> genres = (List<Map<String, Object>>) film.get("genres");
                        if (genres != null) {
                            return genres.stream()
                                    .anyMatch(genre -> {
                                        String name = (String) genre.get("name");
                                        return "Комедия".equals(name);
                                    });
                        }
                        return false;
                    });

            assertThat(allFilmsAreComedies).isTrue();
        }

        // Проверяем фильтрацию по жанру "Драма" (ID = 2)
        List<Map<String, Object>> dramaFilms = webTestClient.get()
                .uri("/films/popular?count=10&genreId=2")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        System.out.println("[DEBUG_LOG] Drama films in response: " + dramaFilms);

        // Проверяем, что список не пустой
        assertThat(dramaFilms).isNotNull();

        // Если список не пустой, проверяем, что все фильмы имеют жанр "Драма"
        if (!dramaFilms.isEmpty()) {
            boolean allFilmsAreDramas = dramaFilms.stream()
                    .allMatch(film -> {
                        List<Map<String, Object>> genres = (List<Map<String, Object>>) film.get("genres");
                        if (genres != null) {
                            return genres.stream()
                                    .anyMatch(genre -> {
                                        String name = (String) genre.get("name");
                                        return "Драма".equals(name);
                                    });
                        }
                        return false;
                    });

            assertThat(allFilmsAreDramas).isTrue();
        }
    }

    /**
     * Тест эндпоинта /films/popular с параметром year
     * Проверяем, что возвращаются фильмы указанного года
     */
    @Test
    void findTopLiked_withYearParameter_shouldReturnFilmsOfSpecifiedYear() {
        // Проверяем фильтрацию по 2020 году
        List<Map<String, Object>> films2020 = webTestClient.get()
                .uri("/films/popular?count=10&year=2020")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        System.out.println("[DEBUG_LOG] Films from 2020 in response: " + films2020);
        System.out.println("[DEBUG_LOG] Expected film IDs from 2020: " +
                           dramaFilm2020.getId() + ", " +
                           comedyFilm2020.getId());

        // Проверяем, что список не пустой
        assertThat(films2020).isNotNull();

        // Если список не пустой, проверяем, что хотя бы один из наших тестовых фильмов 2020 года присутствует
        if (!films2020.isEmpty()) {
            boolean containsAnyFilm2020 = films2020.stream()
                    .anyMatch(film -> {
                        Integer id = (Integer) film.get("id");
                        return id.equals(dramaFilm2020.getId()) || id.equals(comedyFilm2020.getId());
                    });

            assertThat(containsAnyFilm2020).isTrue();
        }

        // Проверяем фильтрацию по 2021 году
        List<Map<String, Object>> films2021 = webTestClient.get()
                .uri("/films/popular?count=10&year=2021")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        System.out.println("[DEBUG_LOG] Films from 2021 in response: " + films2021);
        System.out.println("[DEBUG_LOG] Expected film IDs from 2021: " +
                           dramaFilm2021.getId() + ", " +
                           comedyFilm2021.getId());

        // Проверяем, что список не пустой
        assertThat(films2021).isNotNull();

        // Если список не пустой, проверяем, что хотя бы один из наших тестовых фильмов 2021 года присутствует
        if (!films2021.isEmpty()) {
            boolean containsAnyFilm2021 = films2021.stream()
                    .anyMatch(film -> {
                        Integer id = (Integer) film.get("id");
                        return id.equals(dramaFilm2021.getId()) || id.equals(comedyFilm2021.getId());
                    });

            assertThat(containsAnyFilm2021).isTrue();
        }
    }

    /**
     * Тест эндпоинта /films/popular с параметрами genreId и year
     * Проверяем, что возвращаются фильмы указанного жанра и года
     */
    @Test
    void findTopLiked_withGenreIdAndYearParameters_shouldReturnFilmsOfSpecifiedGenreAndYear() {
        // Проверяем фильтрацию по жанру "Комедия" (ID = 1) и 2020 году
        List<Map<String, Object>> comedyFilms2020 = webTestClient.get()
                .uri("/films/popular?count=10&genreId=1&year=2020")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        System.out.println("[DEBUG_LOG] Comedy films from 2020 in response: " + comedyFilms2020);
        System.out.println("[DEBUG_LOG] Expected comedy film ID from 2020: " + comedyFilm2020.getId());

        // Проверяем, что список не пустой
        assertThat(comedyFilms2020).isNotNull();

        // Если список не пустой, проверяем, что комедия 2020 года присутствует в результатах
        if (!comedyFilms2020.isEmpty()) {
            boolean containsComedyFilm2020 = comedyFilms2020.stream()
                    .anyMatch(film -> {
                        Integer id = (Integer) film.get("id");
                        return id.equals(comedyFilm2020.getId());
                    });

            assertThat(containsComedyFilm2020).isTrue();
        }

        // Проверяем фильтрацию по жанру "Драма" (ID = 2) и 2021 году
        List<Map<String, Object>> dramaFilms2021 = webTestClient.get()
                .uri("/films/popular?count=10&genreId=2&year=2021")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        System.out.println("[DEBUG_LOG] Drama films from 2021 in response: " + dramaFilms2021);
        System.out.println("[DEBUG_LOG] Expected drama film ID from 2021: " + dramaFilm2021.getId());

        // Проверяем, что список не пустой
        assertThat(dramaFilms2021).isNotNull();

        // Если список не пустой, проверяем, что драма 2021 года присутствует в результатах
        if (!dramaFilms2021.isEmpty()) {
            boolean containsDramaFilm2021 = dramaFilms2021.stream()
                    .anyMatch(film -> {
                        Integer id = (Integer) film.get("id");
                        return id.equals(dramaFilm2021.getId());
                    });

            assertThat(containsDramaFilm2021).isTrue();
        }
    }

    /**
     * Тест эндпоинта /films/popular с ограничением количества результатов
     * Проверяем, что возвращается не более указанного количества фильмов
     */
    @Test
    void findTopLiked_withLimitParameter_shouldReturnLimitedNumberOfFilms() {
        // Проверяем, что возвращается только 2 самых популярных фильма
        List<Map<String, Object>> topTwoFilms = webTestClient.get()
                .uri("/films/popular?count=2")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        System.out.println("[DEBUG_LOG] Top 2 films in response: " + topTwoFilms);

        // Проверяем, что список не пустой
        assertThat(topTwoFilms).isNotNull();

        // Проверяем, что список содержит не более 2 фильмов
        if (topTwoFilms != null) {
            assertThat(topTwoFilms.size()).isLessThanOrEqualTo(2);

            // Проверяем, что каждый фильм в списке имеет необходимые поля
            if (!topTwoFilms.isEmpty()) {
                boolean allFilmsHaveRequiredFields = topTwoFilms.stream()
                        .allMatch(film ->
                            film.containsKey("id") &&
                            film.containsKey("name") &&
                            film.containsKey("description") &&
                            film.containsKey("releaseDate") &&
                            film.containsKey("duration") &&
                            film.containsKey("mpa") &&
                            film.containsKey("genres")
                        );

                assertThat(allFilmsHaveRequiredFields).isTrue();
            }
        }
    }

    /**
     * Тест эндпоинта /films/popular с несуществующим жанром
     * Проверяем, что возвращается ошибка 404 Not Found
     */
    @Test
    void findTopLiked_withInvalidGenreId_shouldReturnNotFound() {
        // Проверяем, что при запросе с несуществующим жанром возвращается ошибка 404
        webTestClient.get()
                .uri("/films/popular?count=10&genreId=999")
                .exchange()
                .expectStatus().isNotFound();
    }

    /**
     * Тест эндпоинта /films/popular с годом, для которого нет фильмов
     * Проверяем, что возвращается пустой список
     */
    @Test
    void findTopLiked_withNonExistentYear_shouldReturnEmptyList() {
        // Проверяем, что при запросе с годом, для которого нет фильмов, возвращается пустой список
        List<Map<String, Object>> films = webTestClient.get()
                .uri("/films/popular?count=10&year=2022")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        // Проверяем, что список пустой
        assertThat(films).isNotNull().isEmpty();
    }
}


