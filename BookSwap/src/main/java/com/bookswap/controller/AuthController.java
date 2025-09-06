package com.bookswap.controller;

import com.bookswap.entity.User;
import com.bookswap.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Контроллер для аутентификации и регистрации пользователей
 * Отвечает только за обработку HTTP запросов, бизнес-логика в UserService
 */
@Controller
@RequestMapping
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final UserService userService;
    
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Страница входа
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
            logger.warn("Failed login attempt");
        }
        
        if (logout != null) {
            model.addAttribute("logout", "You have been logged out successfully");
            logger.info("User logged out");
        }
        
        return "login";
    }
    
    /**
     * Страница регистрации - GET
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    
    /**
     * Обработка регистрации - POST
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                              BindingResult bindingResult,
                              @RequestParam("confirmPassword") String confirmPassword,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        logger.info("Registration attempt for username: {}", user.getUsername());
        
        // Валидация формы
        if (bindingResult.hasErrors()) {
            logger.warn("Registration form validation errors for user: {}", user.getUsername());
            return "register";
        }
        
        // Делегируем валидацию и регистрацию сервису
        try {
            String validationResult = userService.validateRegistration(user, confirmPassword);
            if (validationResult != null) {
                model.addAttribute("error", validationResult);
                return "register";
            }
            
            userService.registerUser(user);
            
            logger.info("User successfully registered: {}", user.getUsername());
            redirectAttributes.addFlashAttribute("success", 
                "Registration successful! You can now log in.");
            
            return "redirect:/login";
            
        } catch (Exception e) {
            logger.error("Registration error for user {}: {}", user.getUsername(), e.getMessage());
            model.addAttribute("error", "Registration failed. Please try again.");
            return "register";
        }
    }
    
    /**
     * Страница "Доступ запрещен"
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
    
    /**
     * Страница восстановления пароля
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }
    
    /**
     * Обработка восстановления пароля
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                      RedirectAttributes redirectAttributes) {
        
        logger.info("Password reset request for email: {}", email);
        
        try {
            boolean result = userService.initiatePasswordReset(email);
            
            if (result) {
                redirectAttributes.addFlashAttribute("success", 
                    "Password reset instructions sent to your email");
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Email not found in our system");
            }
            
        } catch (Exception e) {
            logger.error("Password reset error for email {}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("error", 
                "An error occurred. Please try again later.");
        }
        
        return "redirect:/forgot-password";
    }
} 