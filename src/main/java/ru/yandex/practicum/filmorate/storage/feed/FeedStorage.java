package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.Feed;

import java.util.List;

public interface FeedStorage {
    List<Feed> findAll();

    List<Feed> findById(Integer id);

    Integer getLikeId(Integer filmId, Integer userId);

    Integer getFriendId(Integer userIdA, Integer userIdB);

    void save(Feed feed);
}
