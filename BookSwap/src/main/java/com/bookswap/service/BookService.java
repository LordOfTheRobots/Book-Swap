package com.bookswap.service;

import com.bookswap.entity.Book;
import com.bookswap.entity.User;
import com.bookswap.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    
    private final BookRepository bookRepository;
    
    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    // CRUD операции
    
    public Book createBook(Book book) {
        logger.info("Создание новой книги: {}", book.getTitle());
        
        Book savedBook = bookRepository.save(book);
        logger.info("Книга успешно создана с ID: {}", savedBook.getId());
        return savedBook;
    }
    
    @Transactional(readOnly = true)
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }
    
    public Book updateBook(Book book) {
        logger.info("Обновление книги с ID: {}", book.getId());
        
        if (!bookRepository.existsById(book.getId())) {
            throw new IllegalArgumentException("Книга не найдена");
        }
        
        Book updatedBook = bookRepository.save(book);
        logger.info("Книга успешно обновлена");
        return updatedBook;
    }
    
    public void deleteBook(Long id) {
        logger.info("Удаление книги с ID: {}", id);
        
        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Книга не найдена");
        }
        
        bookRepository.deleteById(id);
        logger.info("Книга успешно удалена");
    }
    
    // Бизнес-логика
    
    @Transactional(readOnly = true)
    public List<Book> findBooksByOwner(User owner) {
        return bookRepository.findByOwner(owner);
    }
    
    @Transactional(readOnly = true)
    public Page<Book> searchBooksByTitle(String title, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCase(title, pageable);
    }
    
    @Transactional(readOnly = true)
    public List<Book> findBooksByStatus(Book.ExchangeStatus status) {
        return bookRepository.findByExchangeStatus(status);
    }
    
    @Transactional(readOnly = true)
    public Optional<Book> findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }
    
    @Transactional(readOnly = true)
    public List<Book> findBooksByAuthor(String authorName) {
        return bookRepository.findByAuthorName(authorName);
    }
    
    @Transactional(readOnly = true)
    public Page<Book> findBooksByGenre(String genreName, Pageable pageable) {
        return bookRepository.findByGenre(genreName, pageable);
    }
    
    @Transactional(readOnly = true)
    public List<Book> findAvailableBooksForUser(Long userId) {
        return bookRepository.findAvailableBooksForUser(userId);
    }
    
    @Transactional(readOnly = true)
    public List<Book> findBooksWithHighRating(Double minRating) {
        return bookRepository.findBooksWithHighRating(minRating);
    }
    
    @Transactional(readOnly = true)
    public List<Book> findPopularAvailableBooks(int reviewCount) {
        return bookRepository.findPopularAvailableBooks(reviewCount);
    }
    
    @Transactional(readOnly = true)
    public Page<Book> searchBooks(String title, String author, String genre, Pageable pageable) {
        return bookRepository.searchBooks(title, author, genre, pageable);
    }
    
    @Transactional(readOnly = true)
    public List<Book> findBooksByPublicationYear(Integer startYear, Integer endYear) {
        return bookRepository.findByPublicationYearBetween(startYear, endYear);
    }
    
    @Transactional(readOnly = true)
    public Page<Book> findBooksByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return bookRepository.findByEstimatedPriceBetween(minPrice, maxPrice, pageable);
    }
    
    @Transactional(readOnly = true)
    public List<Book> findBooksByLanguage(String language) {
        return bookRepository.findByLanguage(language);
    }
    
    // Управление статусом книги
    
    public void markBookAsAvailable(Long bookId) {
        logger.info("Отметка книги как доступной: {}", bookId);
        
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setExchangeStatus(Book.ExchangeStatus.AVAILABLE);
            bookRepository.save(book);
            logger.info("Книга отмечена как доступная: {}", book.getTitle());
        }
    }
    
    public void markBookAsReserved(Long bookId) {
        logger.info("Резервирование книги: {}", bookId);
        
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setExchangeStatus(Book.ExchangeStatus.RESERVED);
            bookRepository.save(book);
            logger.info("Книга зарезервирована: {}", book.getTitle());
        }
    }
    
    public void markBookAsExchanged(Long bookId) {
        logger.info("Отметка книги как обмененной: {}", bookId);
        
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setExchangeStatus(Book.ExchangeStatus.EXCHANGED);
            bookRepository.save(book);
            logger.info("Книга отмечена как обмененная: {}", book.getTitle());
        }
    }
    
    public void markBookAsNotAvailable(Long bookId) {
        logger.info("Отметка книги как недоступной: {}", bookId);
        
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setExchangeStatus(Book.ExchangeStatus.NOT_AVAILABLE);
            bookRepository.save(book);
            logger.info("Книга отмечена как недоступная: {}", book.getTitle());
        }
    }
    
    // Статистические методы
    
    @Transactional(readOnly = true)
    public long countBooksByStatus(Book.ExchangeStatus status) {
        return bookRepository.countByExchangeStatus(status);
    }
    
    @Transactional(readOnly = true)
    public List<Object[]> getBookStatsByLanguage() {
        return bookRepository.getBookStatsByLanguage();
    }
    
    @Transactional(readOnly = true)
    public List<Object[]> getMostPopularGenres() {
        return bookRepository.getMostPopularGenres();
    }
    
    @Transactional(readOnly = true)
    public Object[] getBookRatingStats(Long bookId) {
        return bookRepository.getBookRatingStats(bookId);
    }
} 