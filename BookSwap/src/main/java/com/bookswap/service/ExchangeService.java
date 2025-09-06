package com.bookswap.service;

import com.bookswap.entity.Book;
import com.bookswap.entity.BookExchange;
import com.bookswap.entity.User;
import com.bookswap.exception.BookSwapException;
import com.bookswap.repository.BookExchangeRepository;
import com.bookswap.repository.BookRepository;
import com.bookswap.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для управления обменами книг
 * Содержит бизнес-логику для процессов обмена
 */
@Service
@Transactional
public class ExchangeService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeService.class);

    private final BookExchangeRepository exchangeRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Autowired
    public ExchangeService(BookExchangeRepository exchangeRepository,
                          BookRepository bookRepository,
                          UserRepository userRepository) {
        this.exchangeRepository = exchangeRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    /**
     * Создание запроса на обмен
     */
    public BookExchange createExchangeRequest(Long bookId, String requesterUsername) {
        logger.info("Creating exchange request for book {} by user {}", bookId, requesterUsername);

        // Получаем книгу
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> BookSwapException.bookNotFound(bookId));

        // Получаем пользователя-запросчика
        User requester = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> BookSwapException.userNotFound(null));

        // Бизнес-логика валидации
        validateExchangeRequest(book, requester);

        // Создаем запрос на обмен
        BookExchange exchange = new BookExchange();
        exchange.setBook(book);
        exchange.setOwner(book.getOwner());
        exchange.setRequester(requester);
        exchange.setStatus(BookExchange.ExchangeStatus.PENDING);
        exchange.setExchangeType(BookExchange.ExchangeType.BOOK_FOR_BOOK);

        // Изменяем статус книги
        book.setExchangeStatus(Book.ExchangeStatus.RESERVED);
        bookRepository.save(book);

        // Сохраняем обмен
        BookExchange savedExchange = exchangeRepository.save(exchange);

        logger.info("Exchange request created with ID: {}", savedExchange.getId());
        return savedExchange;
    }

    /**
     * Одобрение запроса на обмен
     */
    public BookExchange approveExchange(Long exchangeId, String ownerUsername) {
        logger.info("Approving exchange {} by owner {}", exchangeId, ownerUsername);

        BookExchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new BookSwapException("EXCHANGE_NOT_FOUND", 
                    "Exchange not found", org.springframework.http.HttpStatus.NOT_FOUND));

        // Проверяем права доступа
        if (!exchange.getOwner().getUsername().equals(ownerUsername)) {
            throw BookSwapException.unauthorizedAccess();
        }

        // Проверяем статус
        if (exchange.getStatus() != BookExchange.ExchangeStatus.PENDING) {
            throw new BookSwapException("INVALID_EXCHANGE_STATUS", 
                "Cannot approve exchange in current status", 
                org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        // Изменяем статус обмена
        exchange.setStatus(BookExchange.ExchangeStatus.ACCEPTED);

        // Сохраняем изменения
        BookExchange savedExchange = exchangeRepository.save(exchange);

        logger.info("Exchange {} approved", exchangeId);
        return savedExchange;
    }

    /**
     * Завершение обмена
     */
    public BookExchange completeExchange(Long exchangeId, String ownerUsername) {
        logger.info("Completing exchange {} by owner {}", exchangeId, ownerUsername);

        BookExchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new BookSwapException("EXCHANGE_NOT_FOUND", 
                    "Exchange not found", org.springframework.http.HttpStatus.NOT_FOUND));

        // Проверяем права доступа
        if (!exchange.getOwner().getUsername().equals(ownerUsername)) {
            throw BookSwapException.unauthorizedAccess();
        }

        // Проверяем статус
        if (exchange.getStatus() != BookExchange.ExchangeStatus.ACCEPTED) {
            throw new BookSwapException("INVALID_EXCHANGE_STATUS", 
                "Cannot complete exchange in current status", 
                org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        // Завершаем обмен
        exchange.setStatus(BookExchange.ExchangeStatus.COMPLETED);
        exchange.setCompleted(true);
        exchange.setExchangeDate(LocalDateTime.now());

        // Изменяем статус книги
        Book book = exchange.getBook();
        book.setExchangeStatus(Book.ExchangeStatus.EXCHANGED);
        bookRepository.save(book);

        // Сохраняем обмен
        BookExchange savedExchange = exchangeRepository.save(exchange);

        logger.info("Exchange {} completed", exchangeId);
        return savedExchange;
    }

    /**
     * Отклонение запроса на обмен
     */
    public void rejectExchange(Long exchangeId, String ownerUsername, String reason) {
        logger.info("Rejecting exchange {} by owner {}", exchangeId, ownerUsername);

        BookExchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new BookSwapException("EXCHANGE_NOT_FOUND", 
                    "Exchange not found", org.springframework.http.HttpStatus.NOT_FOUND));

        // Проверяем права доступа
        if (!exchange.getOwner().getUsername().equals(ownerUsername)) {
            throw BookSwapException.unauthorizedAccess();
        }

        // Изменяем статус обмена
        exchange.setStatus(BookExchange.ExchangeStatus.REJECTED);
        if (reason != null) {
            exchange.setOwnerResponse(reason);
        }

        // Возвращаем книгу в доступные
        Book book = exchange.getBook();
        book.setExchangeStatus(Book.ExchangeStatus.AVAILABLE);
        bookRepository.save(book);

        // Сохраняем изменения
        exchangeRepository.save(exchange);

        logger.info("Exchange {} rejected", exchangeId);
    }

    /**
     * Получение обменов пользователя
     */
    @Transactional(readOnly = true)
    public Page<BookExchange> getUserExchanges(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BookSwapException.userNotFound(null));

        return exchangeRepository.findByRequesterOrOwner(user, user, pageable);
    }

    /**
     * Получение входящих запросов пользователя
     */
    @Transactional(readOnly = true)
    public List<BookExchange> getIncomingRequests(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BookSwapException.userNotFound(null));

        return exchangeRepository.findPendingExchangesForOwner(user.getId());
    }

    /**
     * Получение исходящих запросов пользователя
     */
    @Transactional(readOnly = true)
    public List<BookExchange> getOutgoingRequests(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BookSwapException.userNotFound(null));

        return exchangeRepository.findByRequester(user);
    }

    /**
     * Валидация запроса на обмен
     */
    private void validateExchangeRequest(Book book, User requester) {
        // Проверяем доступность книги
        if (book.getExchangeStatus() != Book.ExchangeStatus.AVAILABLE) {
            throw BookSwapException.bookNotAvailable(book.getId());
        }

        // Проверяем, что пользователь не запрашивает свою книгу
        if (book.getOwner().equals(requester)) {
            throw BookSwapException.invalidExchangeRequest();
        }
    }
} 