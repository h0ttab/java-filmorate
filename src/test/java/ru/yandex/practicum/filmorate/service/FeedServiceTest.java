package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.yandex.practicum.filmorate.model.FeedEventType.*;
import static ru.yandex.practicum.filmorate.model.OperationType.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FeedServiceTest {
    private final FeedService feedService;

    @Test
    public void testFindById() {
        List<Feed> feeds = feedService.findById(1);

        assertThat(feeds)
                .hasSize(3)
                .extracting(Feed::getEventId)
                .containsExactlyInAnyOrder(1, 2, 3);

        Feed feedFirst = feeds.get(0);
        Feed feedSecond = feeds.get(1);
        Feed feedThird = feeds.get(2);

        assertThat(feedFirst.getEventType()).isEqualTo(LIKE);
        assertThat(feedSecond.getEventType()).isEqualTo(REVIEW);
        assertThat(feedThird.getEventType()).isEqualTo(FRIEND);
        assertThat(feedFirst.getOperation()).isEqualTo(REMOVE);
        assertThat(feedSecond.getOperation()).isEqualTo(UPDATE);
        assertThat(feedThird.getOperation()).isEqualTo(ADD);
        assertThat(feedFirst.getEntityId()).isEqualTo(333);
        assertThat(feedSecond.getEntityId()).isEqualTo(1);
        assertThat(feedThird.getEntityId()).isEqualTo(2);
    }

    @Test
    public void testFindAll() {
        List<Feed> feeds = feedService.findAll();

        assertThat(feeds)
                .hasSize(4)
                .extracting(Feed::getEventId)
                .containsExactlyInAnyOrder(1, 2, 3, 4);

        Feed feed = feeds.get(3);

        assertThat(feed.getEventType()).isEqualTo(REVIEW);
        assertThat(feed.getOperation()).isEqualTo(ADD);
        assertThat(feed.getEntityId()).isEqualTo(3);
    }

    @Test
    public void testSaveFeed() {
        List<Feed> feeds = feedService.findAll();

        assertThat(feeds)
                .hasSize(4)
                .extracting(Feed::getEventId)
                .containsExactlyInAnyOrder(1, 2, 3, 4);

        Feed feed = new Feed(Instant.now().toEpochMilli(), 3, FRIEND, ADD,2);

        feedService.save(feed.getUserId(), feed.getEventType(), feed.getOperation(), feed.getEntityId());
        Feed saveFeed = feedService.findById(3).getLast();
        feed.setEventId(saveFeed.getEventId());

        assertThat(feed).isEqualTo(saveFeed);
        List<Feed> feedsAfterSave = feedService.findAll();

        assertThat(feedsAfterSave)
                .hasSize(5)
                .extracting(Feed::getEventId)
                .containsExactlyInAnyOrder(1, 2, 3, 4, feed.getEventId());
    }
}