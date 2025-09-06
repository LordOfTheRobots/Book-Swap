package com.bookswap.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для всего приложения
 * Обеспечивает централизованную обработку ошибок и безопасность
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Обработка ошибок валидации
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.warn("Validation error: {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка исключений "сущность не найдена"
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(
            EntityNotFoundException ex, HttpServletRequest request) {
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "Resource not found");
        error.put("message", ex.getMessage());
        error.put("path", request.getRequestURI());
        
        logger.warn("Entity not found: {} at {}", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Обработка ошибок доступа (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        
        logger.warn("Access denied for user at {}: {}", request.getRequestURI(), ex.getMessage());
        
        ModelAndView mav = new ModelAndView("error/403");
        mav.addObject("error", "Access Denied");
        mav.addObject("message", "You don't have permission to access this resource");
        return mav;
    }

    /**
     * Обработка исключений недопустимых аргументов
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid argument");
        error.put("message", ex.getMessage());
        error.put("path", request.getRequestURI());
        
        logger.warn("Illegal argument: {} at {}", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка общих исключений
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        logger.error("Unexpected error at {}: ", request.getRequestURI(), ex);
        
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("error", "Internal Server Error");
        mav.addObject("message", "An unexpected error occurred");
        return mav;
    }

    /**
     * Обработка пользовательских исключений BookSwap
     */
    @ExceptionHandler(BookSwapException.class)
    public ResponseEntity<Map<String, String>> handleBookSwapException(
            BookSwapException ex, HttpServletRequest request) {
        
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getErrorCode());
        error.put("message", ex.getMessage());
        error.put("path", request.getRequestURI());
        
        logger.warn("BookSwap error: {} - {} at {}", 
                ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
        
        return new ResponseEntity<>(error, ex.getStatus());
    }
} 