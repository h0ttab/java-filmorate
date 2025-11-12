package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.feed.FeedDbStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedDbStorage feedDbStorage;

    public Collection<Feed> findAll() {
        return feedDbStorage.findAll();
    }

    public Collection<Feed> findById(Integer id) {
        return feedDbStorage.findById(id);
    }

    public Integer getLikeId(Integer filmId, Integer userId) {
        return feedDbStorage.getLikeId(filmId, userId);
    }

    public Integer getFriendId(Integer userIdA, Integer userIdB) {
        return feedDbStorage.getFriendId(userIdA, userIdB);
    }
    public void save(Feed feed) {
        this.feedDbStorage.save(feed);
    }
}