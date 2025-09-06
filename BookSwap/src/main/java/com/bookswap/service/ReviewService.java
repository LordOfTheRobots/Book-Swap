package com.bookswap.service;

import com.bookswap.entity.Book;
import com.bookswap.entity.Review;
import com.bookswap.entity.User;
import com.bookswap.exception.BookSwapException;
import com.bookswap.repository.BookRepository;
import com.bookswap.repository.ReviewRepository;
import com.bookswap.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для управления отзывами о книгах
 * Содержит бизнес-логику для создания и получения отзывов
 */
@Service
@Transactional
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                        BookRepository bookRepository,
                        UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    /**
     * Создание нового отзыва
     */
    public Review createReview(Long bookId, String username, Integer rating, String comment) {
        logger.info("Creating review for book {} by user {}", bookId, username);

        // Валидация входных данных
        validateReviewData(rating, comment);

        // Получаем книгу и пользователя
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> BookSwapException.bookNotFound(bookId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BookSwapException.userNotFound(null));

        // Проверяем, что пользователь не оставляет отзыв на свою книгу
        if (book.getOwner().equals(user)) {
            throw new BookSwapException("REVIEW_OWN_BOOK", 
                "Cannot review your own book", 
                org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        // Создаем отзыв
        Review review = new Review();
        review.setBook(book);
        review.setUser(user);
        review.setRating(rating);
        review.setContent(comment);

        Review savedReview = reviewRepository.save(review);
        logger.info("Review created with ID: {}", savedReview.getId());
        return savedReview;
    }

    /**
     * Обновление отзыва
     */
    public Review updateReview(Long reviewId, String username, Integer rating, String comment) {
        logger.info("Updating review {} by user {}", reviewId, username);

        // Валидация входных данных
        validateReviewData(rating, comment);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BookSwapException("REVIEW_NOT_FOUND", 
                    "Review not found", 
                    org.springframework.http.HttpStatus.NOT_FOUND));

        // Проверяем права доступа
        if (!review.getUser().getUsername().equals(username)) {
            throw BookSwapException.unauthorizedAccess();
        }

        // Обновляем данные
        review.setRating(rating);
        review.setContent(comment);

        Review savedReview = reviewRepository.save(review);
        logger.info("Review {} updated", reviewId);
        return savedReview;
    }

    /**
     * Удаление отзыва
     */
    public void deleteReview(Long reviewId, String username) {
        logger.info("Deleting review {} by user {}", reviewId, username);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BookSwapException("REVIEW_NOT_FOUND", 
                    "Review not found", 
                    org.springframework.http.HttpStatus.NOT_FOUND));

        // Проверяем права доступа
        if (!review.getUser().getUsername().equals(username)) {
            throw BookSwapException.unauthorizedAccess();
        }

        reviewRepository.delete(review);
        logger.info("Review {} deleted", reviewId);
    }

    /**
     * Получение отзывов для книги
     */
    @Transactional(readOnly = true)
    public Page<Review> getBookReviews(Long bookId, Pageable pageable) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> BookSwapException.bookNotFound(bookId));

        return reviewRepository.findByBookAndApprovedTrue(book, pageable);
    }

    /**
     * Получение отзывов пользователя
     */
    @Transactional(readOnly = true)
    public List<Review> getUserReviews(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BookSwapException.userNotFound(null));

        return reviewRepository.findByUser(user);
    }

    /**
     * Получение последних отзывов
     */
    @Transactional(readOnly = true)
    public List<Review> getLatestReviews(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return reviewRepository.findAll(pageable).getContent();
    }

    /**
     * Валидация данных отзыва
     */
    private void validateReviewData(Integer rating, String comment) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BookSwapException("INVALID_RATING", 
                "Rating must be between 1 and 5", 
                org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        if (comment != null && comment.length() > 2000) {
            throw new BookSwapException("COMMENT_TOO_LONG", 
                "Comment cannot exceed 2000 characters", 
                org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }
} 