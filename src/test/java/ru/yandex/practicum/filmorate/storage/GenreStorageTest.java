package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(GenreDbStorage.class)
public class GenreStorageTest {
    private final GenreDbStorage storage;

    @Test
    public void testFindById() {
        Genre genre = storage.findById(1);

        assertThat(genre)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Комедия");
    }

    @Test
    public void testFindAll() {
        List<Genre> genres = storage.findAll();

        assertThat(genres.size()).isEqualTo(6);
        assertThat(genres).allSatisfy(genre -> {
            assertThat(genre)
                    .hasFieldOrProperty("name")
                    .hasFieldOrProperty("id")
                    .hasNoNullFieldsOrProperties();
        });
    }

    @Test
    public void testFindGenreByFilmId() {
        List<Integer> genreIds = storage.findGenreByFilmId(1).stream().mapToInt(Genre::getId).boxed().toList();

        assertThat(genreIds).containsAll(List.of(2, 6));
    }

    @Test
    public void linkGenresToFilm() {
        storage.linkGenresToFilm(1, Set.of(3), false);
        List<Integer> genreIds = storage.findGenreByFilmId(1).stream().mapToInt(Genre::getId).boxed().toList();
        assertThat(genreIds).containsAll(List.of(2, 6, 3));
    }
}