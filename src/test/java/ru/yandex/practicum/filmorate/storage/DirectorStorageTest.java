package ru.yandex.practicum.filmorate.storage;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(DirectorDbStorage.class)
public class DirectorStorageTest {
    private final DirectorDbStorage storage;

    @Test
    public void testCreateDirector() {
        Director director = Director.builder().name("Имя Режиссёра").build();
        Director created = storage.create(director);
        Director foundById = storage.findById(6);
        director.setId(6);

        assertThat(director).isEqualTo(created).isEqualTo(foundById);
    }

    @Test
    public void testFindAll() {
        List<Director> directorList = storage.findAll();

        assertThat(directorList)
                .hasSize(5)
                .first().isEqualTo(Director.builder().id(1).name("Роберт Земекис").build());
    }

    @Test
    public void testFindByFilm() {
        List<Director> directors = storage.findByFilm(3);

        assertThat(directors).containsExactly(
                Director.builder().id(3).name("Эндрю Адамсон").build(),
                Director.builder().id(4).name("Вики Дженсон").build()
        );
    }

    @Test
    public void testFindById() {
        Director director = storage.findById(5);

        assertThat(director)
                .hasFieldOrPropertyWithValue("id", 5)
                .hasFieldOrPropertyWithValue("name", "Томми Ли Уоллес");
    }

    @Test
    public void testUpdate() {
        Director director = storage.findById(2);
        assertThat(director)
                .hasFieldOrPropertyWithValue("id", 2)
                .hasFieldOrPropertyWithValue("name", "Квентин Тарантино");

        storage.update(director.toBuilder().name("Твентин Карантино").build());

        assertThat(storage.findById(2))
                .hasFieldOrPropertyWithValue("id", 2)
                .hasFieldOrPropertyWithValue("name", "Твентин Карантино");
    }

    @Test
    public void testLinkDirectorsToFilm() {
        Director directorOriginal = storage.findByFilm(1).getFirst();
        Director directorToAdd = storage.findById(5);

        assertThat(storage.findByFilm(1)).hasSize(1);
        assertThat(directorOriginal)
                .hasFieldOrPropertyWithValue("id", 2)
                .hasFieldOrPropertyWithValue("name", "Квентин Тарантино");

        storage.linkDirectorsToFilm(1, List.of(5), false);

        List<Director> directorsAfterUpdate = storage.findByFilm(1);

        assertThat(directorsAfterUpdate)
                .hasSize(2)
                .contains(directorOriginal, directorToAdd);
    }

    @Test
    public void testDelete() {
        Assertions.assertDoesNotThrow(() -> storage.findById(1));
        storage.delete(1);
        Assertions.assertThrows(NotFoundException.class, () -> storage.findById(1));
    }
}
