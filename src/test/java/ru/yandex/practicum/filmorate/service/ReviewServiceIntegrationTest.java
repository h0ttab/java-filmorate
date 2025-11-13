package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.dto.review.ReviewCreateDto;
import ru.yandex.practicum.filmorate.model.dto.review.ReviewUpdateDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class ReviewServiceIntegrationTest {
    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReviewCreateDto.ReviewCreateDtoBuilder defaultReviewBuilder() {
        return ReviewCreateDto.builder()
                .content("Новый отзыв о фильме")
                .isPositive(true)
                .userId(1)
                .filmId(1);
    }

    @Test
    void createReviewShouldInitializeUsefulToZero() {
        Review review = reviewService.create(defaultReviewBuilder().build());
        Review stored = reviewService.findById(review.getReviewId());
        assertThat(stored.getUseful()).isZero();
    }

    @Test
    void addLikeShouldIncreaseUseful() {
        Review review = reviewService.create(defaultReviewBuilder().build());
        reviewService.addLike(review.getReviewId(), 2);
        Review updated = reviewService.findById(review.getReviewId());
        assertThat(updated.getUseful()).isEqualTo(1);
    }

    @Test
    void addDislikeShouldDecreaseUseful() {
        Review review = reviewService.create(defaultReviewBuilder().build());
        reviewService.addDislike(review.getReviewId(), 3);
        Review updated = reviewService.findById(review.getReviewId());
        assertThat(updated.getUseful()).isEqualTo(-1);
    }

    @Test
    void removeLikeShouldRestoreUseful() {
        Review review = reviewService.create(defaultReviewBuilder().build());
        reviewService.addLike(review.getReviewId(), 2);
        reviewService.removeLike(review.getReviewId(), 2);
        Review updated = reviewService.findById(review.getReviewId());
        assertThat(updated.getUseful()).isZero();
    }

    @Test
    void removeDislikeShouldRestoreUseful() {
        Review review = reviewService.create(defaultReviewBuilder().build());
        reviewService.addDislike(review.getReviewId(), 3);
        reviewService.removeDislike(review.getReviewId(), 3);
        Review updated = reviewService.findById(review.getReviewId());
        assertThat(updated.getUseful()).isZero();
    }

    @Test
    void findAllShouldBeSortedByUsefulDesc_forGivenFilmOnly() {
        Review positiveReview = reviewService.create(
                defaultReviewBuilder()
                        .content("Понравилось")
                        .isPositive(true)
                        .build()
        );

        Review negativeReview = reviewService.create(
                defaultReviewBuilder()
                        .content("Не понравилось")
                        .isPositive(false)
                        .build()
        );

        reviewService.addLike(positiveReview.getReviewId(), 2);
        reviewService.addDislike(negativeReview.getReviewId(), 3);

        List<Review> all = reviewService.findAll(1, 10);

        List<Review> reviews = all.stream()
                .filter(r -> List.of(
                        positiveReview.getReviewId(),
                        negativeReview.getReviewId()
                ).contains(r.getReviewId()))
                .toList();

        assertThat(reviews)
                .extracting(Review::getReviewId)
                .containsExactly(
                        positiveReview.getReviewId(),
                        negativeReview.getReviewId()
                );

        assertThat(reviews)
                .extracting(Review::getUseful)
                .containsExactly(1, -1);
    }


    @Test
    void deleteShouldCascadeFeedback() {
        Review review = reviewService.create(defaultReviewBuilder().build());
        reviewService.addLike(review.getReviewId(), 2);
        Integer countBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM review_feedback WHERE review_id = ?",
                Integer.class,
                review.getReviewId());
        assertThat(countBefore).isEqualTo(1);

        reviewService.delete(review.getReviewId());
        Integer countAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM review_feedback WHERE review_id = ?",
                Integer.class,
                review.getReviewId());
        assertThat(countAfter).isZero();
    }

    @Test
    void invalidReviewIdShouldThrowNotFound() {
        assertThatThrownBy(() -> reviewService.findById(9999)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> reviewService.addLike(9999, 1)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void invalidUserIdShouldThrowNotFound() {
        Review review = reviewService.create(defaultReviewBuilder().build());
        assertThatThrownBy(() -> reviewService.addLike(review.getReviewId(), 9999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateShouldModifyProvidedFields() {
        Review review = reviewService.create(defaultReviewBuilder().build());
        ReviewUpdateDto updateDto = ReviewUpdateDto.builder()
                .reviewId(review.getReviewId())
                .content("Обновленный текст")
                .build();

        Review updated = reviewService.update(updateDto);

        assertThat(updated.getContent()).isEqualTo("Обновленный текст");

        Review fromDb = reviewService.findById(review.getReviewId());
        assertThat(fromDb).usingRecursiveComparison().isEqualTo(updated);
    }
}
