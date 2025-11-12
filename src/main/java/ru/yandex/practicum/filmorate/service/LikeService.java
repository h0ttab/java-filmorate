package ru.yandex.practicum.filmorate.service;

import java.time.Instant;
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
        Feed feed = new Feed(Instant.now().toEpochMilli(), userId, Event.LIKE.toString(), Operation.ADD.toString(), feedService.getLikeId(filmId, userId));
        feedService.save(feed);
    }

    public void removeLike(Integer filmId, Integer userId) {
        validators.validateLikeExists(filmId, userId, getClass());
        Integer likeId = feedService.getLikeId(filmId, userId);
        likeStorage.removeLike(filmId, userId);
        if (likeId != null) {
            Feed feed = new Feed(Instant.now().toEpochMilli(), userId, Event.LIKE.toString(), Operation.REMOVE.toString(), likeId);
            feedService.save(feed);
        }
    }

    public List<Integer> getLikesByFilmId(Integer filmId) {
        return likeStorage.getLikesByFilmId(filmId);
    }
}