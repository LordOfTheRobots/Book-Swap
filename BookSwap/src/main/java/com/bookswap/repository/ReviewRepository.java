package com.bookswap.repository;

import com.bookswap.entity.Book;
import com.bookswap.entity.Review;
import com.bookswap.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByBook(Book book);
    
    List<Review> findByUser(User user);
    
    List<Review> findByApprovedTrue();
    
    List<Review> findByApprovedFalse();
    
    Page<Review> findByBookAndApprovedTrue(Book book, Pageable pageable);
    
    Optional<Review> findByUserAndBook(User user, Book book);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book = :book AND r.approved = true")
    Double findAverageRatingByBook(@Param("book") Book book);
    
    @Query("SELECT r FROM Review r WHERE r.rating >= :minRating AND r.approved = true")
    List<Review> findHighRatedReviews(@Param("minRating") Integer minRating);
} 