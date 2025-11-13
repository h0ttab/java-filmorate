DELETE FROM review_feedback;

DELETE FROM review;
ALTER TABLE review ALTER COLUMN id RESTART WITH 1;

DELETE FROM mpa;
ALTER TABLE mpa ALTER COLUMN id RESTART WITH 1;

DELETE FROM genre;
ALTER TABLE genre ALTER COLUMN id RESTART WITH 1;

DELETE FROM friends;
ALTER TABLE friends ALTER COLUMN id RESTART WITH 1;

DELETE FROM "like";
ALTER TABLE "like" ALTER COLUMN id RESTART WITH 1;

DELETE FROM film_genre;
ALTER TABLE film_genre ALTER COLUMN id RESTART WITH 1;

DELETE FROM "user";
ALTER TABLE "user" ALTER COLUMN id RESTART WITH 1;

DELETE FROM film;
ALTER TABLE film ALTER COLUMN id RESTART WITH 1;

DELETE FROM film_director;
ALTER TABLE film_director ALTER COLUMN id RESTART WITH 1;

DELETE FROM director;
ALTER TABLE director ALTER COLUMN id RESTART WITH 1;

DELETE FROM feed;
ALTER TABLE feed ALTER COLUMN id RESTART WITH 1;

DELETE FROM film_director;
ALTER TABLE film_director ALTER COLUMN id RESTART WITH 1;

DELETE FROM director;
ALTER TABLE director ALTER COLUMN id RESTART WITH 1;

INSERT INTO mpa (name) VALUES
('G'),
('PG'),
('PG-13'),
('R'),
('NC-17');

INSERT INTO genre (name) VALUES
('Комедия'),
('Драма'),
('Мультфильм'),
('Триллер'),
('Документальный'),
('Боевик');

INSERT INTO director (name) VALUES
('Роберт Земекис'),
('Квентин Тарантино'),
('Эндрю Адамсон'),
('Вики Дженсон'),
('Томми Ли Уоллес');

INSERT INTO "user" (email, login, name, birthday) VALUES
('ivan.petrov@example.com', 'ivan_p', 'Иван Петров', '1990-05-15'),
('maria.sidorova@example.com', 'maria_s', 'Мария Сидорова', '1995-08-20'),
('alex.smirnov@example.com', 'alex_s', 'Алексей Смирнов', '1988-12-01');

INSERT INTO film (name, description, release_date, duration, mpa_id) VALUES
('Криминальное чтиво', 'Фильм Квентина Тарантино о двух бандитах.', '1994-10-14', 154, 4),
('Форрест Гамп', 'История жизни необычного человека.', '1994-07-06', 142, 3),
('Шрек', 'История про зеленого огра и его приключения.', '2001-05-18', 90, 2);

INSERT INTO film_genre (film_id, genre_id) VALUES
(1, 2), -- Криминальное чтиво (ID=1) -> Драма (ID=2)
(1, 6), -- Криминальное чтиво (ID=1) -> Боевик (ID=6)
(2, 2), -- Форрест Гамп (ID=2) -> Драма (ID=2)
(3, 3), -- Шрек (ID=3) -> Мультфильм (ID=3)
(3, 1); -- Шрек (ID=3) -> Комедия (ID=1)

INSERT INTO film_director (film_id, director_id) VALUES
(2, 1), -- Форрест Гамп (ID=2) -> Роберт Земекис (ID=1)
(1, 2), -- Криминальное чтиво (ID=1) -> Квентин Тарантино (ID=2)
(3, 3), -- Шрек (ID=3) -> Эндрю Адамсон (ID=3)
(3, 4); -- Шрек (ID=3) -> Вики Дженсон (ID=4)

INSERT INTO "like" (user_id, film_id) VALUES
(1, 1), -- Иван (ID=1) лайкнул "Криминальное чтиво" (ID=1)
(2, 1), -- Мария (ID=2) лайкнула "Криминальное чтиво" (ID=1)
(3, 2), -- Алексей (ID=3) лайкнул "Форрест Гамп" (ID=2)
(2, 3), -- Мария (ID=2) лайкнула "Шрек" (ID=3)
(1, 3), -- Иван (ID=1) лайкнул "Шрек" (ID=3);
(3, 3); -- Алексей (ID=3) лайкнул "Шрек" (ID=3)

INSERT INTO friends (request_from_id, request_to_id, is_accepted) VALUES
(1, 2, TRUE),  -- Иван (ID=1) и Мария (ID=2) - друзья (заявка от Ивана подтверждена)
(1, 3, FALSE),  -- Иван (ID=1) отправил заявку Алексею (ID=3), но она еще не принята
(2, 1, TRUE),  -- Мария (ID=2) и Иван (ID=1) - друзья (взаимная заявка от Марии подтверждена)
(3, 2, FALSE); -- Алексей (ID=3) отправил заявку Марии (ID=2), но она еще не принята

INSERT INTO review (content, is_positive, user_id, film_id, useful) VALUES
('Отличный сюжет и игра актеров!', TRUE, 1, 1, 1),
('Ожидал большего от фильма.', FALSE, 2, 2, -1);

INSERT INTO review_feedback (review_id, user_id, is_useful) VALUES
(1, 2, TRUE),
(2, 3, FALSE);

INSERT INTO feed (date, user_id, event_type, operation, entity_id) VALUES
(NOW() - INTERVAL '4' DAY, 1, 'LIKE',   'REMOVE', 333),
(NOW() - INTERVAL '3' DAY, 1, 'FILM',   'UPDATE', 1),
(NOW() - INTERVAL '2' DAY, 1, 'FRIEND', 'ADD', 2);