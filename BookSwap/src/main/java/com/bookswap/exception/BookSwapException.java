package com.bookswap.exception;

import org.springframework.http.HttpStatus;

/**
 * Пользовательское исключение для приложения BookSwap
 * Содержит код ошибки и HTTP статус для более детальной обработки
 */
public class BookSwapException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public BookSwapException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public BookSwapException(String errorCode, String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    // Предопределенные исключения для частых случаев

    public static BookSwapException bookNotFound(Long bookId) {
        return new BookSwapException(
                "BOOK_NOT_FOUND",
                "Book with ID " + bookId + " not found",
                HttpStatus.NOT_FOUND
        );
    }

    public static BookSwapException userNotFound(Long userId) {
        return new BookSwapException(
                "USER_NOT_FOUND",
                "User with ID " + userId + " not found",
                HttpStatus.NOT_FOUND
        );
    }

    public static BookSwapException bookNotAvailable(Long bookId) {
        return new BookSwapException(
                "BOOK_NOT_AVAILABLE",
                "Book with ID " + bookId + " is not available for exchange",
                HttpStatus.CONFLICT
        );
    }

    public static BookSwapException unauthorizedAccess() {
        return new BookSwapException(
                "UNAUTHORIZED_ACCESS",
                "You don't have permission to perform this action",
                HttpStatus.FORBIDDEN
        );
    }

    public static BookSwapException invalidExchangeRequest() {
        return new BookSwapException(
                "INVALID_EXCHANGE_REQUEST",
                "Cannot create exchange request for your own book",
                HttpStatus.BAD_REQUEST
        );
    }

    public static BookSwapException externalApiError(String apiName, String details) {
        return new BookSwapException(
                "EXTERNAL_API_ERROR",
                "Error calling " + apiName + " API: " + details,
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }
} 