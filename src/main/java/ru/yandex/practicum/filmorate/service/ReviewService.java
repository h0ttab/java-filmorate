package ru.yandex.practicum.filmorate.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.dto.review.ReviewCreateDto;
import ru.yandex.practicum.filmorate.model.dto.review.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.util.DtoHelper;
import ru.yandex.practicum.filmorate.util.Validators;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final Validators validators;
    private final ReviewMapper mapper;
    private final DtoHelper dtoHelper;

    public Review create(ReviewCreateDto dto) {
        Review review = mapper.toEntity(dto);
        return reviewStorage.create(review);
    }

    public Review update(ReviewUpdateDto dto) {
        validators.validateReviewExists(dto.getReviewId(), getClass());
        Review original = reviewStorage.findById(dto.getReviewId());
        Review patch = mapper.toEntity(dto);
        Review merged = (Review) dtoHelper.transferFields(original, patch);
        return reviewStorage.update(merged);
    }

    public void delete(Integer id) {
        validators.validateReviewExists(id, getClass());
        reviewStorage.delete(id);
    }

    public Review findById(Integer id) {
        validators.validateReviewExists(id, getClass());
        return reviewStorage.findById(id);
    }

    public List<Review> findAll(Integer filmId, Integer count) {
        int limit = (count == null || count <= 0) ? 10 : count;
        if (filmId != null) {
            validators.validateFilmExists(filmId, getClass());
        }
        return reviewStorage.findAll(filmId, limit);
    }

    public void addLike(Integer reviewId, Integer userId) {
        validators.validateReviewExists(reviewId, getClass());
        validators.validateUserExits(userId, getClass());
        reviewStorage.addUseful(reviewId, userId);
    }

    public void addDislike(Integer reviewId, Integer userId) {
        validators.validateReviewExists(reviewId, getClass());
        validators.validateUserExits(userId, getClass());
        reviewStorage.addUseless(reviewId, userId);
    }

    public void removeLike(Integer reviewId, Integer userId) {
        validators.validateReviewFeedbackExists(reviewId, userId, getClass());
        reviewStorage.removeUseful(reviewId, userId);
    }

    public void removeDislike(Integer reviewId, Integer userId) {
        validators.validateReviewFeedbackExists(reviewId, userId, getClass());
        reviewStorage.removeUseless(reviewId, userId);
    }
}
