-- Инициализация тестовых данных для BookSwap

-- Добавление тестовых пользователей
INSERT INTO users (username, password, email, first_name, last_name, city, role, enabled, created_at) VALUES
('admin', '$2a$10$YQDQbWGGqRwEa0TlECX.veSEP4fjw6GW0XqG6EZU1XhFQsM.YJUvm', 'admin@bookswap.com', 'Admin', 'User', 'Moscow', 'ADMIN', true, CURRENT_TIMESTAMP),
('john_doe', '$2a$10$YQDQbWGGqRwEa0TlECX.veSEP4fjw6GW0XqG6EZU1XhFQsM.YJUvm', 'john@example.com', 'John', 'Doe', 'Saint Petersburg', 'USER', true, CURRENT_TIMESTAMP),
('jane_smith', '$2a$10$YQDQbWGGqRwEa0TlECX.veSEP4fjw6GW0XqG6EZU1XhFQsM.YJUvm', 'jane@example.com', 'Jane', 'Smith', 'Novosibirsk', 'USER', true, CURRENT_TIMESTAMP),
('book_lover', '$2a$10$YQDQbWGGqRwEa0TlECX.veSEP4fjw6GW0XqG6EZU1XhFQsM.YJUvm', 'lover@books.com', 'Alex', 'Reader', 'Yekaterinburg', 'USER', true, CURRENT_TIMESTAMP),
('reader_mike', '$2a$10$YQDQbWGGqRwEa0TlECX.veSEP4fjw6GW0XqG6EZU1XhFQsM.YJUvm', 'mike@reading.com', 'Mike', 'Johnson', 'Kazan', 'USER', true, CURRENT_TIMESTAMP);

-- Добавление авторов
INSERT INTO authors (name, biography, birth_date) VALUES
('George Orwell', 'English novelist and essayist, journalist and critic, best known for his novels 1984 and Animal Farm.', '1903-06-25'),
('J.K. Rowling', 'British author, best known for the Harry Potter series of fantasy novels.', '1965-07-31'),
('Stephen King', 'American author of horror, supernatural fiction, suspense, science fiction, and fantasy novels.', '1947-09-21'),
('Agatha Christie', 'English writer known for her detective novels, particularly those featuring Hercule Poirot and Miss Marple.', '1890-09-15'),
('Isaac Asimov', 'American writer and professor of biochemistry, known for his works of science fiction and popular science.', '1920-01-02'),
('Douglas Adams', 'English author, screenwriter, essayist, humorist, satirist and dramatist.', '1952-03-11'),
('Ray Bradbury', 'American author and screenwriter known for his science fiction and fantasy works.', '1920-08-22'),
('Arthur Conan Doyle', 'British writer and physician, most noted for creating the fictional detective Sherlock Holmes.', '1859-05-22');

-- Добавление жанров
INSERT INTO genres (name, description) VALUES
('Science Fiction', 'Fiction dealing with futuristic concepts such as advanced technology, space exploration, time travel, parallel universes.'),
('Fantasy', 'Fiction set in a fictional universe, often featuring magic, magical creatures, and mythical beings.'),
('Mystery', 'Fiction dealing with the solution of a crime or the unraveling of secrets.'),
('Horror', 'Fiction intended to frighten, unsettle, or create suspense.'),
('Romance', 'Fiction that focuses on relationships and romantic love between characters.'),
('Thriller', 'Fiction characterized by excitement, suspense, and high emotion.'),
('Historical Fiction', 'Fiction set in the past, often featuring period details and historical events.'),
('Biography', 'Non-fiction focusing on the life story of a particular person.'),
('Self-Help', 'Non-fiction intended to help readers improve their lives in some way.'),
('Philosophy', 'Non-fiction exploring fundamental questions about existence, knowledge, values, reason, mind, and language.');

-- Добавление книг
INSERT INTO books (title, isbn, description, publication_year, language, status, owner_id, created_at) VALUES
('1984', '978-0-452-28423-4', 'A dystopian social science fiction novel by George Orwell, about a future society under totalitarian rule.', 1949, 'English', 'AVAILABLE', 2, CURRENT_TIMESTAMP),
('Animal Farm', '978-0-14-036121-8', 'An allegorical novella by George Orwell reflecting events leading up to the Russian Revolution of 1917.', 1945, 'English', 'AVAILABLE', 2, CURRENT_TIMESTAMP),
('Harry Potter and the Philosopher''s Stone', '978-0-7475-3269-9', 'The first novel in the Harry Potter series by J.K. Rowling.', 1997, 'English', 'AVAILABLE', 3, CURRENT_TIMESTAMP),
('The Shining', '978-0-385-12167-5', 'A horror novel by Stephen King about a family''s descent into madness.', 1977, 'English', 'EXCHANGED', 3, CURRENT_TIMESTAMP),
('Murder on the Orient Express', '978-0-00-711932-3', 'A detective novel by Agatha Christie featuring Hercule Poirot.', 1934, 'English', 'AVAILABLE', 4, CURRENT_TIMESTAMP),
('Foundation', '978-0-553-29335-0', 'A science fiction novel by Isaac Asimov, first in the Foundation series.', 1951, 'English', 'PENDING', 4, CURRENT_TIMESTAMP),
('The Hitchhiker''s Guide to the Galaxy', '978-0-345-39180-3', 'A comedy science fiction series by Douglas Adams.', 1979, 'English', 'AVAILABLE', 5, CURRENT_TIMESTAMP),
('Fahrenheit 451', '978-1-4516-7331-9', 'A dystopian novel by Ray Bradbury about a future society where books are banned.', 1953, 'English', 'AVAILABLE', 5, CURRENT_TIMESTAMP),
('The Adventures of Sherlock Holmes', '978-0-14-043916-5', 'A collection of twelve detective stories by Arthur Conan Doyle.', 1892, 'English', 'AVAILABLE', 2, CURRENT_TIMESTAMP),
('Harry Potter and the Chamber of Secrets', '978-0-439-06486-6', 'The second novel in the Harry Potter series by J.K. Rowling.', 1998, 'English', 'EXCHANGED', 3, CURRENT_TIMESTAMP);

-- Связи между книгами и авторами (Many-to-Many)
INSERT INTO book_authors (book_id, author_id) VALUES
(1, 1), (2, 1), -- George Orwell
(3, 2), (10, 2), -- J.K. Rowling  
(4, 3), -- Stephen King
(5, 4), -- Agatha Christie
(6, 5), -- Isaac Asimov
(7, 6), -- Douglas Adams
(8, 7), -- Ray Bradbury
(9, 8); -- Arthur Conan Doyle

-- Связи между книгами и жанрами (Many-to-Many)
INSERT INTO book_genres (book_id, genre_id) VALUES
(1, 1), (1, 6), -- 1984: Science Fiction, Thriller
(2, 1), (2, 7), -- Animal Farm: Science Fiction, Historical Fiction
(3, 2), -- Harry Potter: Fantasy
(4, 4), (4, 6), -- The Shining: Horror, Thriller
(5, 3), -- Murder on the Orient Express: Mystery
(6, 1), -- Foundation: Science Fiction
(7, 1), -- Hitchhiker's Guide: Science Fiction
(8, 1), (8, 4), -- Fahrenheit 451: Science Fiction, Horror
(9, 3), -- Sherlock Holmes: Mystery
(10, 2); -- Harry Potter 2: Fantasy

-- Добавление состояний книг (One-to-One)
INSERT INTO book_conditions (book_id, condition_type, description, rating) VALUES
(1, 'EXCELLENT', 'Brand new condition, never read', 5),
(2, 'GOOD', 'Minor wear on cover, pages in good condition', 4),
(3, 'FAIR', 'Some wear and tear, readable but shows use', 3),
(4, 'EXCELLENT', 'Like new, carefully maintained', 5),
(5, 'GOOD', 'Light wear, very readable', 4),
(6, 'FAIR', 'Well-loved copy with some damage', 3),
(7, 'EXCELLENT', 'Perfect condition', 5),
(8, 'GOOD', 'Minor corner bending, otherwise great', 4),
(9, 'EXCELLENT', 'Pristine condition', 5),
(10, 'FAIR', 'Student copy with some highlighting', 3);

-- Добавление отзывов (Many-to-One)
INSERT INTO reviews (book_id, user_id, rating, comment, created_at) VALUES
(1, 3, 5, 'A masterpiece! Orwell''s vision is more relevant than ever.', CURRENT_TIMESTAMP),
(1, 4, 4, 'Thought-provoking and disturbing. Everyone should read this.', CURRENT_TIMESTAMP),
(3, 2, 5, 'Amazing start to an incredible series. Perfect for all ages.', CURRENT_TIMESTAMP),
(3, 4, 5, 'Magic, friendship, and adventure. What more could you want?', CURRENT_TIMESTAMP),
(5, 2, 4, 'Classic Christie mystery. Couldn''t put it down!', CURRENT_TIMESTAMP),
(7, 3, 5, 'Hilarious and brilliant. Adams at his best.', CURRENT_TIMESTAMP),
(8, 4, 4, 'Powerful commentary on censorship and society.', CURRENT_TIMESTAMP),
(9, 5, 5, 'The original detective stories. Still the best!', CURRENT_TIMESTAMP);

-- Добавление обменов (Many-to-One relationships)
INSERT INTO book_exchanges (book_id, owner_id, requester_id, status, requested_at, completed_at, notes) VALUES
(4, 3, 2, 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 'Great exchange! Book was in excellent condition.'),
(10, 3, 4, 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 'Quick and easy exchange. Highly recommend this user.'),
(6, 4, 5, 'PENDING', CURRENT_TIMESTAMP - INTERVAL '2 days', NULL, 'Interested in this classic sci-fi book.'),
(1, 2, 5, 'REQUESTED', CURRENT_TIMESTAMP - INTERVAL '1 day', NULL, 'Would love to read this dystopian classic.');

-- Обновление статусов книг в соответствии с обменами
UPDATE books SET status = 'EXCHANGED' WHERE id IN (4, 10);
UPDATE books SET status = 'PENDING' WHERE id = 6; 