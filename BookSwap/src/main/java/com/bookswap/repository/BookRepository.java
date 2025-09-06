package com.bookswap.repository;

import com.bookswap.entity.Book;
import com.bookswap.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    // Spring Data JPA методы
    List<Book> findByOwner(User owner);
    
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    List<Book> findByExchangeStatus(Book.ExchangeStatus status);
    
    Optional<Book> findByIsbn(String isbn);
    
    List<Book> findByPublicationYearBetween(Integer startYear, Integer endYear);
    
    Page<Book> findByEstimatedPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    List<Book> findByLanguage(String language);
    
    // Кастомные запросы с @Query
    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.lastName LIKE %:authorName%")
    List<Book> findByAuthorName(@Param("authorName") String authorName);
    
    @Query("SELECT b FROM Book b JOIN b.genres g WHERE g.name = :genreName")
    Page<Book> findByGenre(@Param("genreName") String genreName, Pageable pageable);
    
    @Query("SELECT b FROM Book b WHERE b.exchangeStatus = 'AVAILABLE' AND " +
           "b.owner.id != :userId ORDER BY b.createdAt DESC")
    List<Book> findAvailableBooksForUser(@Param("userId") Long userId);
    
    // Запрос с подзапросом - книги с высоким рейтингом
    @Query("SELECT b FROM Book b WHERE b.id IN " +
           "(SELECT r.book.id FROM Review r WHERE r.approved = true " +
           "GROUP BY r.book.id HAVING AVG(r.rating) >= :minRating)")
    List<Book> findBooksWithHighRating(@Param("minRating") Double minRating);
    
    // Сложный запрос с агрегацией
    @Query("SELECT b FROM Book b WHERE SIZE(b.reviews) > :reviewCount AND " +
           "b.exchangeStatus = 'AVAILABLE'")
    List<Book> findPopularAvailableBooks(@Param("reviewCount") int reviewCount);
    
    // Поиск книг по нескольким критериям
    @Query("SELECT DISTINCT b FROM Book b " +
           "LEFT JOIN b.authors a " +
           "LEFT JOIN b.genres g " +
           "WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:author IS NULL OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :author, '%'))) " +
           "AND (:genre IS NULL OR g.name = :genre) " +
           "AND b.exchangeStatus = 'AVAILABLE'")
    Page<Book> searchBooks(@Param("title") String title,
                          @Param("author") String author,
                          @Param("genre") String genre,
                          Pageable pageable);
    
    // Статистические запросы
    @Query("SELECT COUNT(b) FROM Book b WHERE b.exchangeStatus = :status")
    long countByExchangeStatus(@Param("status") Book.ExchangeStatus status);
    
    @Query("SELECT b.language, COUNT(b) FROM Book b GROUP BY b.language")
    List<Object[]> getBookStatsByLanguage();
    
    @Query("SELECT g.name, COUNT(b) FROM Book b JOIN b.genres g GROUP BY g.name ORDER BY COUNT(b) DESC")
    List<Object[]> getMostPopularGenres();
    
    // Нативный SQL запрос для сложной статистики
    @Query(value = "SELECT AVG(rating) as avg_rating, COUNT(*) as review_count " +
                   "FROM reviews r WHERE r.book_id = :bookId AND r.is_approved = true",
           nativeQuery = true)
    Object[] getBookRatingStats(@Param("bookId") Long bookId);
} 