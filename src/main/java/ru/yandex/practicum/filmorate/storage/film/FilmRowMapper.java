package ru.yandex.practicum.filmorate.storage.film;

import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.*;

@Component
@RequiredArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {
    private final MpaService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;
    private final LikeService likeService;

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = Film.builder()
                .id(resultSet.getInt("ID"))
                .name(resultSet.getString("NAME"))
                .description(resultSet.getString("DESCRIPTION"))
                .releaseDate(resultSet.getDate("RELEASE_DATE").toLocalDate())
                .duration(resultSet.getInt("DURATION"))
                .mpa(mpaService.findById(resultSet.getInt("MPA_ID")))
                .genres(genreService.findByFilmId(resultSet.getInt("ID")))
                .directors(directorService.findByFilm(resultSet.getInt("ID")))
                .build();
        film.getLikes().addAll(likeService.getLikesByFilmId(film.getId()));
        return film;
    }
}