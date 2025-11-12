package ru.yandex.practicum.filmorate.mapper;

import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.dto.ObjectIdDto;
import ru.yandex.practicum.filmorate.model.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.model.dto.film.FilmUpdateDto;
import ru.yandex.practicum.filmorate.service.*;
import ru.yandex.practicum.filmorate.util.Validators;

@Component
@RequiredArgsConstructor
public class FilmMapper {
    private final Validators validators;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;

    public Film toEntity(FilmCreateDto filmCreateDto) {
        validators.validateFilmReleaseDate(filmCreateDto.getReleaseDate(), getClass());
        validators.validateMpaExists(filmCreateDto.getMpa().get().getId(), getClass());

        Film.FilmBuilder film = Film.builder()
                .name(filmCreateDto.getName())
                .description(filmCreateDto.getDescription())
                .duration(filmCreateDto.getDuration());

        film.mpa(mpaService.findById(filmCreateDto.getMpa().get().getId()));
        film.releaseDate(filmCreateDto.getReleaseDate());
        if (filmCreateDto.getDirectors().isPresent()) {
            film.directors(filmCreateDto.getDirectors().get().stream()
                    .map(dto -> directorService.findById(dto.getId())).toList()
            );
        } else {
            film.directors(List.of());
        }
        List<ObjectIdDto> genreDtoList = filmCreateDto.getGenres().orElse(new ArrayList<>());
        ArrayList<Genre> genreList = new ArrayList<>();

        if (!genreDtoList.isEmpty()) {
            for (ObjectIdDto objectIdDto : filmCreateDto.getGenres().get()) {
                validators.validateGenreExists(objectIdDto.getId(), getClass());
            }
            genreDtoList.forEach(genreDto -> genreList.add(genreService.findById(genreDto.getId())));
        }
        film.genres(genreList);

        return film.build();
    }

    public Film toEntity(FilmUpdateDto filmUpdateDto) {
        Film.FilmBuilder filmBuilder = Film.builder()
                .id(filmUpdateDto.getId())
                .name(filmUpdateDto.getName().orElse(null))
                .duration(filmUpdateDto.getDuration().orElse(null))
                .description(filmUpdateDto.getDescription().orElse(null));

        if (Optional.ofNullable(filmUpdateDto.getReleaseDate()).isPresent()) {
            filmBuilder.releaseDate(filmUpdateDto.getReleaseDate());
        }

        if (filmUpdateDto.getDirectors().isPresent()) {
            filmBuilder.directors(filmUpdateDto.getDirectors().get().stream()
                    .map(dto -> directorService.findById(dto.getId())).toList()
            );
        } else {
            filmBuilder.directors(List.of());
        }

        if (filmUpdateDto.getGenres().isPresent()) {
            List<Genre> genresOfFilm = filmUpdateDto.getGenres().get().stream()
                    .mapToInt(ObjectIdDto::getId)
                    .boxed()
                    .peek(genreId -> validators.validateGenreExists(genreId, getClass()))
                    .map(genreService::findById)
                    .toList();
            filmBuilder.genres(new ArrayList<>(genresOfFilm));
        }

        if (filmUpdateDto.getMpa().isPresent()) {
            filmBuilder.mpa(mpaService.findById(filmUpdateDto.getMpa().get().getId()));
        }

        return filmBuilder.build();
    }
}
