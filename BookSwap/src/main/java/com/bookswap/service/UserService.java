package com.bookswap.service;

import com.bookswap.entity.User;
import com.bookswap.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    // CRUD операции
    
    public User createUser(User user) {
        logger.info("Создание нового пользователя: {}", user.getUsername());
        
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        
        logger.info("Пользователь успешно создан с ID: {}", savedUser.getId());
        return savedUser;
    }
    
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    public User updateUser(User user) {
        logger.info("Обновление пользователя с ID: {}", user.getId());
        
        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        
        User updatedUser = userRepository.save(user);
        logger.info("Пользователь успешно обновлен");
        return updatedUser;
    }
    
    public void deleteUser(Long id) {
        logger.info("Удаление пользователя с ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        
        userRepository.deleteById(id);
        logger.info("Пользователь успешно удален");
    }
    
    // Бизнес-логика
    
    public User registerUser(String username, String email, String password, 
                           String firstName, String lastName) {
        logger.info("Регистрация нового пользователя: {}", username);
        
        User user = new User(username, email, password, firstName, lastName);
        return createUser(user);
    }
    
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        logger.info("Смена пароля для пользователя с ID: {}", userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            logger.warn("Неверный старый пароль для пользователя: {}", user.getUsername());
            return false;
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        logger.info("Пароль успешно изменен для пользователя: {}", user.getUsername());
        return true;
    }
    
    public void enableUser(Long userId) {
        logger.info("Активация пользователя с ID: {}", userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEnabled(true);
            userRepository.save(user);
            logger.info("Пользователь активирован: {}", user.getUsername());
        }
    }
    
    public void disableUser(Long userId) {
        logger.info("Деактивация пользователя с ID: {}", userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEnabled(false);
            userRepository.save(user);
            logger.info("Пользователь деактивирован: {}", user.getUsername());
        }
    }
    
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                searchTerm, searchTerm, pageable);
    }
    
    @Transactional(readOnly = true)
    public List<User> findUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }
    
    @Transactional(readOnly = true)
    public List<User> findActiveUsers() {
        return userRepository.findByEnabledTrue();
    }
    
    @Transactional(readOnly = true)
    public List<User> findUsersRegisteredAfter(LocalDateTime date) {
        return userRepository.findUsersRegisteredAfter(date);
    }
    
    @Transactional(readOnly = true)
    public List<User> findUsersWithMoreThanBooks(int bookCount) {
        return userRepository.findUsersWithMoreThanBooks(bookCount);
    }
    
    @Transactional(readOnly = true)
    public List<User> findUsersWithCompletedExchanges() {
        return userRepository.findUsersWithCompletedExchanges();
    }
    
    @Transactional(readOnly = true)
    public List<User> findUsersByBookGenre(String genreName) {
        return userRepository.findUsersByBookGenre(genreName);
    }
    
    // UserDetailsService implementation
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Загрузка пользователя по имени: {}", username);
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Пользователь не найден: {}", username);
                    return new UsernameNotFoundException("Пользователь не найден: " + username);
                });
    }
    
    // Статистические методы
    @Transactional(readOnly = true)
    public long countUsersByRole(User.Role role) {
        return userRepository.countByRole(role);
    }
    
    @Transactional(readOnly = true)
    public List<Object[]> getUserStatsByRole() {
        return userRepository.getUserStatsByRole();
    }

    // Методы валидации для бизнес-логики

    /**
     * Валидация регистрации пользователя
     */
    public String validateRegistration(User user, String confirmPassword) {
        // Проверка имени пользователя
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return "Username is required";
        }
        
        if (user.getUsername().length() < 3 || user.getUsername().length() > 50) {
            return "Username must be between 3 and 50 characters";
        }
        
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Username is already taken";
        }

        // Проверка email
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return "Email is required";
        }
        
        if (!isValidEmail(user.getEmail())) {
            return "Invalid email format";
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            return "Email is already registered";
        }

        // Проверка пароля
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            return "Password must be at least 6 characters long";
        }
        
        if (!user.getPassword().equals(confirmPassword)) {
            return "Passwords do not match";
        }

        // Проверка обязательных полей
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            return "First name is required";
        }
        
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            return "Last name is required";
        }
        
        if (user.getCity() == null || user.getCity().trim().isEmpty()) {
            return "City is required";
        }

        return null; // Валидация прошла успешно
    }

    /**
     * Регистрация пользователя с автоматическим хэшированием пароля
     */
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * Инициация сброса пароля
     */
    public boolean initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            // Здесь можно добавить логику отправки email
            logger.info("Password reset initiated for email: {}", email);
            return true;
        }
        return false;
    }

    /**
     * Простая валидация email
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
} 