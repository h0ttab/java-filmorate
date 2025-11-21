package ru.yandex.practicum.filmorate.storage;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.*;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.feed.FeedDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.util.Validators;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class,
        MpaService.class,
        MpaDbStorage.class,
        Validators.class,
        GenreService.class,
        GenreDbStorage.class,
        FilmMapper.class,
        FilmRowMapper.class,
        FeedDbStorage.class,
        FeedService.class,
        LikeDbStorage.class,
        LikeService.class,
        DirectorService.class,
        DirectorDbStorage.class})
public class FilmStorageTest {
    private final FilmDbStorage storage;

    private void assertFilm(Film film, Integer id, String name, String description, LocalDate releaseDate,
                            Integer duration) {
        assertThat(film)
                .hasFieldOrPropertyWithValue("id", id)
                .hasFieldOrPropertyWithValue("name", name)
                .hasFieldOrPropertyWithValue("description", description)
                .hasFieldOrPropertyWithValue("releaseDate", releaseDate)
                .hasFieldOrPropertyWithValue("duration", duration);
    }

    @Test
    public void testFindById() {
        Film film = storage.findById(3);

        assertFilm(film, 3, "Шрек", "История про зеленого огра и его приключения.",
                LocalDate.of(2001, 5, 18), 90);
    }

    @Test
    public void testFindAll() {
        List<Film> films = storage.findAll();

        assertThat(films)
                .hasSize(3)
                .extracting(Film::getId)
                .containsExactlyInAnyOrder(1, 2, 3);

        films.forEach(film -> {
            if (film.getId() == 3) {
                assertFilm(film, 3, "Шрек", "История про зеленого огра и его приключения.",
                        LocalDate.of(2001, 5, 18), 90);
            }
        });
    }

    @Test
    public void testFilmCreate() {
        Film film = Film.builder()
                .name("Оно")
                .description("Леденящее воплощение ужаса притаилось в тени и повсюду")
                .releaseDate(LocalDate.of(1990, 11, 18)).duration(192)
                .mpa(Mpa.builder().id(1).build())
                .build();
        Film createdFilm = storage.create(film);
        Film createdFilmFromDb = storage.findById(createdFilm.getId());

        assertFilm(createdFilmFromDb, film.getId(), film.getName(),
                film.getDescription(), film.getReleaseDate(), film.getDuration());
    }

    @Test
    public void testFilmUpdate() {
        Film originalFilm = storage.findById(1);
        Film update = originalFilm.toBuilder()
                .name("UPDATED NAME")
                .build();

        assertThat(originalFilm)
                .hasFieldOrPropertyWithValue("name", "Криминальное чтиво");

        storage.update(update);

        Film updatedFilm = storage.findById(1);

        assertThat(updatedFilm)
                .hasFieldOrPropertyWithValue("name", "UPDATED NAME");
    }

    @Test
    public void testFilmDelete() {
        Assertions.assertDoesNotThrow(() -> storage.findById(1));
        storage.delete(1);
        Assertions.assertThrows(NotFoundException.class, () -> storage.findById(1));
    }

    @Test
    public void testFindTopLiked() {
        List<Film> films = storage.findTopLiked(3);
        assertThat(films).extracting(Film::getId).containsExactly(3, 1, 2);
    }

    @Test
    public void testFindCommonFilms_shouldReturnIntersectionSortedByPopularity() {
        // Иван (id=1) и Мария (id=2) совместно лайкнули фильмы 1 и 3.
        // По данным data.sql: у фильма 3 три лайка, у фильма 1 — два,
        // поэтому порядок по популярности должен быть [3, 1].
        List<Film> films = storage.findCommonFilms(1, 2);

        assertThat(films)
                .extracting(Film::getId)
                .containsExactly(3, 1);
    }

    /**
     * Тест метода findCommonFilms:
     * при отсутствии общих лайков должен возвращаться пустой список.
     * <p>
     * Здесь второй пользователь (id=999) не имеет ни одной записи в таблице "like",
     * поэтому пересечение лайкнутых фильмов пользователя 1 и пользователя 999 пустое.
     * Проверка существования пользователя лежит на сервисном слое, а не на хранилище.
     */
    @Test
    public void testFindCommonFilms_whenNoCommonLikes_shouldReturnEmptyList() {
        List<Film> films = storage.findCommonFilms(1, 999);

        assertThat(films).isEmpty();
    }
}
