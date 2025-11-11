package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.util.Validators;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeDbStorage likeStorage;
    private final Validators validators;
    private final FeedService feedService;

    public void addLike(Integer filmId, Integer userId) {
        validators.validateLikeNotExists(filmId, userId, getClass());
        likeStorage.addLike(filmId, userId);
        Feed feed = new Feed(LocalDate.now(), userId, Event.LIKE.toString(), Operation.ADD.toString(), 111);
        feedService.save(feed);
    }

    public void removeLike(Integer filmId, Integer userId) {
        validators.validateLikeExists(filmId, userId, getClass());
        likeStorage.removeLike(filmId, userId);
    }

    public List<Integer> getLikesByFilmId(Integer filmId) {
        return likeStorage.getLikesByFilmId(filmId);
    }
}