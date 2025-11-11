package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Feed {
    private LocalDate timestamp;
    private Integer userId;
    private String eventType;
    private String operation;
    private Integer eventId;
    private Integer entityId;

    public Feed() {
    }

    public Feed(LocalDate timestamp, Integer userId, String eventType, String operation,
                Integer entityId) {
        this.timestamp = timestamp;
        this.userId = userId;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
    }

    public Feed(final Integer eventId, final LocalDate timestamp, final Integer userId, final String eventType,
                final String operation,
                final Integer entityId) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.userId = userId;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
    }
}