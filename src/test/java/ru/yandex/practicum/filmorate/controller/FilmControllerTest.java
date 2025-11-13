package ru.yandex.practicum.filmorate.controller;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.filmorate.config.ControllerTest;
import ru.yandex.practicum.filmorate.model.*;

@ControllerTest
public class FilmControllerTest {
    @Autowired
    private WebTestClient webTestClient;

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
}


