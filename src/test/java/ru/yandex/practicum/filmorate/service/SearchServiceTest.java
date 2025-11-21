package ru.yandex.practicum.filmorate.service;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.feed.FeedDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.search.SearchDb;
import ru.yandex.practicum.filmorate.util.DtoHelper;
import ru.yandex.practicum.filmorate.util.Validators;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        FilmDbStorage.class,
        MpaService.class,
        MpaDbStorage.class,
        Validators.class,
        GenreService.class,
        GenreDbStorage.class,
        FilmMapper.class,
        FeedService.class,
        FeedDbStorage.class,
        LikeDbStorage.class,
        LikeService.class,
        DirectorService.class,
        DirectorDbStorage.class,
        SearchService.class,
        SearchDb.class,
        FilmService.class,
        DtoHelper.class
})
public class SearchServiceTest {
    private final SearchService searchService;
    private final FilmService filmService;

    @Test
    public void searchByTitleTest() {
        Film filmById = filmService.findById(2);
        assertThat(filmById).hasFieldOrPropertyWithValue("name", "Форрест Гамп");

        Film filmByTitle = searchService.searchFilms("рест", Set.of("title")).getFirst();
        assertThat(filmById).isEqualTo(filmByTitle);
    }

    @Test
    public void searchByDirectorTest() {
        Film filmById = filmService.findById(2);
        assertThat(filmById).hasFieldOrPropertyWithValue(
                "directors", List.of(Director.builder().id(1).name("Роберт Земекис").build())
        );

        Film filmByTitle = searchService.searchFilms("берт", Set.of("director")).getFirst();
        assertThat(filmById).isEqualTo(filmByTitle);
    }

    @Test
    public void searchByTitleAndDirectorTest() {
        Film filmById = filmService.findById(2);
        assertThat(filmById).hasFieldOrPropertyWithValue(
                "directors", List.of(Director.builder().id(1).name("Роберт Земекис").build())
        );

        // Ожидается, что по названию найдёт "ШрЕК", а по режиссёру - Роберта ЗемЕКиса (Форрест Гамп)
        List<Film> films = searchService.searchFilms("ек", Set.of("director", "title"));
        assertThat(films).containsExactly(
                filmService.findById(3),
                filmService.findById(2)
        );
    }

    @Test
    public void multipleResultTest() {
        List<Film> films = List.of(filmService.findById(1), filmService.findById(2));
        assertThat(films).extracting(Film::getName).contains("Криминальное чтиво", "Форрест Гамп");

        List<Film> searchResult = searchService.searchFilms("м", Set.of("title"));
        assertThat(films).isEqualTo(searchResult);
    }

    @Test
    public void resultOrderTest() {
        List<Film> films = filmService.findTopLiked(3);
        assertThat(films).extracting(Film::getName)
                .containsExactly("Шрек", "Криминальное чтиво", "Форрест Гамп");

        List<Film> searchResult = searchService.searchFilms("е", Set.of("title"));
        assertThat(searchResult).extracting(Film::getName)
                .containsExactly("Шрек", "Криминальное чтиво", "Форрест Гамп");
    }
}
