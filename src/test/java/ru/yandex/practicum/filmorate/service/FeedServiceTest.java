package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class FeedServiceTest {
    @Autowired
    private FeedService feedService;

    private void assertFeed(Feed feed, Long timestamp, Integer userId, String eventType, String operation,
                            Integer entityId) {
        assertThat(feed)
                .hasFieldOrPropertyWithValue("timestamp", timestamp)
                .hasFieldOrPropertyWithValue("userId", userId)
                .hasFieldOrPropertyWithValue("eventType", eventType)
                .hasFieldOrPropertyWithValue("operation", operation)
                .hasFieldOrPropertyWithValue("entityId", entityId);
    }

    @Test
    public void testFindById() {
        List<Feed> feeds = feedService.findById(1);

        assertThat(feeds)
                .hasSize(3)
                .extracting(Feed::getEventId)
                .containsExactlyInAnyOrder(1, 2, 3);

        feeds.forEach(feed -> {
            if (feed.getEventId() == 1) {
                assertFeed(feed,
                        915138000000L,
                        1,
                        Event.LIKE.toString(),
                        Operation.REMOVE.toString(),
                        333);
            }
        });
    }

    @Test
    public void testFindAll() {
        List<Feed> feeds = feedService.findAll();

        assertThat(feeds)
                .hasSize(4)
                .extracting(Feed::getEventId)
                .containsExactlyInAnyOrder(1, 2, 3, 4);

        feeds.forEach(feed -> {
            if (feed.getEventId() == 4) {
                assertFeed(feed,
                        1588626000000L,
                        2,
                        Event.REVIEW.toString(),
                        Operation.ADD.toString(),
                        3);
            }
        });
    }

    @Test
    public void testSaveFeed() {
        List<Feed> feeds = feedService.findAll();

        assertThat(feeds)
                .hasSize(4)
                .extracting(Feed::getEventId)
                .containsExactlyInAnyOrder(1, 2, 3, 4);

        Feed feed = new Feed(915138000000L, 3, Event.FRIEND.toString(), Operation.ADD.toString(),
                2);

        feedService.save(feed);
        Feed saveFeed = feedService.findById(3).getLast();
        feed.setEventId(saveFeed.getEventId());
        assertThat(feed).isEqualTo(saveFeed);

        List<Feed> feedsAfterSave = feedService.findAll();

        assertThat(feedsAfterSave)
                .hasSize(5)
                .extracting(Feed::getEventId)
                .containsExactlyInAnyOrder(1, 2, 3, 4, 5);
    }
}