package ru.yandex.practicum.filmorate.storage.like;

import java.util.List;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage.LikeBatchDto;

public interface LikeStorage {
    void addLike(Integer filmId, Integer userId);

    void removeLike(Integer filmId, Integer userId);

    List<Integer> getLikesByFilmId(Integer filmId);

    List<LikeBatchDto> getLikesByFilmIdList(List<Integer> filmIdList);
}