package ru.yandex.practicum.filmorate.service;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage.LikeBatchDto;
import ru.yandex.practicum.filmorate.util.Validators;

import static ru.yandex.practicum.filmorate.model.FeedEventType.LIKE;
import static ru.yandex.practicum.filmorate.model.OperationType.ADD;
import static ru.yandex.practicum.filmorate.model.OperationType.REMOVE;

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
        feedService.save(userId, LIKE, ADD, filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        validators.validateLikeExists(filmId, userId, getClass());
        likeStorage.removeLike(filmId, userId);
        feedService.save(userId, LIKE, REMOVE, filmId);
    }

    public List<Integer> getLikesByFilmId(Integer filmId) {
        return likeStorage.getLikesByFilmId(filmId);
    }

    public Map<Integer, List<Integer>> getLikesByFilmIdList(List<Integer> filmIdList) {
        List<LikeBatchDto> likeBatchDtoList = likeStorage.getLikesByFilmIdList(filmIdList);
        Map<Integer, List<Integer>> filmLikeMap = new HashMap<>();
        likeBatchDtoList.forEach(likeBatchDto -> {
            Integer filmId = likeBatchDto.filmId();
            List<Integer> likeUserIdList = Arrays.stream(likeBatchDto.likeList().split(","))
                    .mapToInt(Integer::parseInt)
                    .boxed()
                    .toList();
            filmLikeMap.put(filmId, likeUserIdList);
        });
        return filmLikeMap;
    }
}