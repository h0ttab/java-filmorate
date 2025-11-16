package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.FeedEventType;
import ru.yandex.practicum.filmorate.model.OperationType;

import java.util.List;

public interface FeedStorage {
    List<Feed> findAll();

    List<Feed> findById(Integer id);

    void save(Integer userId, FeedEventType feedEventType, OperationType operationType, Integer entityId);
}
