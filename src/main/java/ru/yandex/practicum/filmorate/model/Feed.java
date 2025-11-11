package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Feed {
    private Integer eventId;
    private LocalDate timestamp;
    private Integer userId;
    private String eventType;
    private String operation;
    private Integer entityId;
}