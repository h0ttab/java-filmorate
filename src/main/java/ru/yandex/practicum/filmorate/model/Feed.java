package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Feed {
    @NonNull
    private Long timestamp;
    @NonNull
    private Integer userId;
    @NonNull
    private FeedEventType eventType;
    @NonNull
    private OperationType operation;
    private Integer eventId;
    @NonNull
    private Integer entityId;

    public Feed() {
    }
}