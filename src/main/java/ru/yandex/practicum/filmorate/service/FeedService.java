package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.FeedEventType;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.storage.feed.FeedDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedDbStorage feedDbStorage;

    public List<Feed> findAll() {
        return feedDbStorage.findAll();
    }

    public List<Feed> findById(Integer id) {
        return feedDbStorage.findById(id);
    }

    public void save(Integer userId, FeedEventType feedEventType, OperationType operationType,
                     Integer entityId) {
        this.feedDbStorage.save(userId, feedEventType, operationType, entityId);
    }
}