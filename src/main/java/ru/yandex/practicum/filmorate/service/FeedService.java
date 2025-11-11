package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.feed.FeedDbStorage;

import java.util.Collection;

@Service
public class FeedService {
    private final FeedDbStorage feedDbStorage;

    @Autowired
    public FeedService(FeedDbStorage feedDbStorage) {
        this.feedDbStorage = feedDbStorage;
    }

    public Collection<Feed> findAll() {
        return feedDbStorage.findAll();
    }
}
