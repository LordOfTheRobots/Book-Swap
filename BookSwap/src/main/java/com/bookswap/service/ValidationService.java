package com.bookswap.service;

import com.bookswap.entity.User;
import com.bookswap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Сервис для централизованной валидации данных
 * Выносит всю логику валидации из контроллеров в отдельный слой
 */
@Service
public class ValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,50}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$");

    private final UserRepository userRepository;

    @Autowired
    public ValidationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Валидация данных пользователя при регистрации
     */
    public ValidationResult validateUserRegistration(User user, String confirmPassword) {
        ValidationResult result = new ValidationResult();

        // Проверка имени пользователя
        if (!StringUtils.hasText(user.getUsername())) {
            result.addError("username", "Username is required");
        } else if (!USERNAME_PATTERN.matcher(user.getUsername()).matches()) {
            result.addError("username", "Username must be 3-50 characters and contain only letters, numbers, and underscores");
        } else if (userRepository.existsByUsername(user.getUsername())) {
            result.addError("username", "Username is already taken");
        }

        // Проверка email
        if (!StringUtils.hasText(user.getEmail())) {
            result.addError("email", "Email is required");
        } else if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            result.addError("email", "Invalid email format");
        } else if (userRepository.existsByEmail(user.getEmail())) {
            result.addError("email", "Email is already registered");
        }

        // Проверка пароля
        if (!StringUtils.hasText(user.getPassword())) {
            result.addError("password", "Password is required");
        } else if (user.getPassword().length() < 6) {
            result.addError("password", "Password must be at least 6 characters long");
        } else if (!PASSWORD_PATTERN.matcher(user.getPassword()).matches()) {
            result.addError("password", "Password must contain at least one letter and one number");
        }

        // Проверка подтверждения пароля
        if (!StringUtils.hasText(confirmPassword)) {
            result.addError("confirmPassword", "Password confirmation is required");
        } else if (!user.getPassword().equals(confirmPassword)) {
            result.addError("confirmPassword", "Passwords do not match");
        }

        // Проверка имени
        if (!StringUtils.hasText(user.getFirstName())) {
            result.addError("firstName", "First name is required");
        } else if (user.getFirstName().length() > 100) {
            result.addError("firstName", "First name cannot exceed 100 characters");
        }

        // Проверка фамилии
        if (!StringUtils.hasText(user.getLastName())) {
            result.addError("lastName", "Last name is required");
        } else if (user.getLastName().length() > 100) {
            result.addError("lastName", "Last name cannot exceed 100 characters");
        }

        // Проверка города
        if (!StringUtils.hasText(user.getCity())) {
            result.addError("city", "City is required");
        } else if (user.getCity().length() > 100) {
            result.addError("city", "City cannot exceed 100 characters");
        }

        return result;
    }

    /**
     * Валидация email адреса
     */
    public boolean isValidEmail(String email) {
        return StringUtils.hasText(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Валидация имени пользователя
     */
    public boolean isValidUsername(String username) {
        return StringUtils.hasText(username) && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Валидация пароля
     */
    public boolean isValidPassword(String password) {
        return StringUtils.hasText(password) && 
               password.length() >= 6 && 
               PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Проверка доступности имени пользователя
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * Проверка доступности email
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * Валидация рейтинга книги
     */
    public boolean isValidRating(Integer rating) {
        return rating != null && rating >= 1 && rating <= 5;
    }

    /**
     * Валидация ISBN
     */
    public boolean isValidISBN(String isbn) {
        if (!StringUtils.hasText(isbn)) {
            return false;
        }
        
        // Удаляем все не-цифры
        String cleanISBN = isbn.replaceAll("[^0-9X]", "");
        
        // Проверяем длину (10 или 13 символов)
        return cleanISBN.length() == 10 || cleanISBN.length() == 13;
    }

    /**
     * Валидация года публикации
     */
    public boolean isValidPublicationYear(Integer year) {
        if (year == null) {
            return false;
        }
        
        int currentYear = java.time.Year.now().getValue();
        return year >= 1000 && year <= currentYear;
    }

    /**
     * Класс для хранения результатов валидации
     */
    public static class ValidationResult {
        private final java.util.Map<String, String> errors = new java.util.HashMap<>();
        
        public void addError(String field, String message) {
            errors.put(field, message);
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public java.util.Map<String, String> getErrors() {
            return errors;
        }
        
        public String getFirstError() {
            return errors.values().stream().findFirst().orElse(null);
        }
    }
} 