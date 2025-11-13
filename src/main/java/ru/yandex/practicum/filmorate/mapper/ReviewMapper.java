package ru.yandex.practicum.filmorate.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.dto.review.ReviewCreateDto;
import ru.yandex.practicum.filmorate.model.dto.review.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.util.Validators;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewMapper {
    private final Validators validators;

    public Review toEntity(ReviewCreateDto dto) {
        validators.validateUserExits(dto.getUserId(), getClass());
        validators.validateFilmExists(dto.getFilmId(), getClass());
        return Review.builder()
                .content(dto.getContent())
                .isPositive(dto.getIsPositive())
                .userId(dto.getUserId())
                .filmId(dto.getFilmId())
                .useful(0)
                .build();
    }

    public Review toEntity(ReviewUpdateDto dto) {
        return Review.builder()
                .reviewId(dto.getReviewId())
                .content(resolveOptional(Optional.ofNullable(dto.getContent())))
                .isPositive(resolveOptional(Optional.ofNullable(dto.getIsPositive())))
                .build();
    }

    private <T> T resolveOptional(Optional<T> value) {
        return value == null ? null : value.orElse(null);
    }
}
