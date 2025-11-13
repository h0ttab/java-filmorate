package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    Integer delete(Integer id);

    Review findById(Integer id);

    List<Review> findAll(Integer filmId, int count);

    void addUseful(Integer reviewId, Integer userId);

    void addUseless(Integer reviewId, Integer userId);

    void removeUseful(Integer reviewId, Integer userId);

    void removeUseless(Integer reviewId, Integer userId);
}
