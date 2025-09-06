package com.bookswap.controller;

import com.bookswap.entity.Book;
import com.bookswap.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Контроллер для главных страниц приложения
 * Отвечает только за обработку HTTP запросов и передачу управления сервисам
 */
@Controller
public class HomeController {
    
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    
    private final BookService bookService;
    
    @Autowired
    public HomeController(BookService bookService) {
        this.bookService = bookService;
    }
    
    @GetMapping("/")
    public String home(Model model) {
        // Получаем последние добавленные книги
        Pageable pageable = PageRequest.of(0, 8, Sort.by("createdAt").descending());
        Page<Book> recentBooks = bookService.findAll(pageable);
        
        // Получаем популярные книги (с высоким рейтингом)
        List<Book> popularBooks = bookService.findBooksWithHighRating(4.0);
        
        // Получаем доступные для обмена книги
        List<Book> availableBooks = bookService.findBooksByStatus(Book.ExchangeStatus.AVAILABLE);
        
        model.addAttribute("recentBooks", recentBooks.getContent());
        model.addAttribute("popularBooks", popularBooks.subList(0, Math.min(6, popularBooks.size())));
        model.addAttribute("availableBooks", availableBooks.subList(0, Math.min(12, availableBooks.size())));
        model.addAttribute("totalBooks", bookService.countBooksByStatus(Book.ExchangeStatus.AVAILABLE));
        
        return "home";
    }
    
    @GetMapping("/books")
    public String books(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Book> booksPage;
        
        // Если есть параметры поиска, используем поиск
        if (title != null || author != null || genre != null) {
            booksPage = bookService.searchBooks(title, author, genre, pageable);
        } else {
            booksPage = bookService.findAll(pageable);
        }
        
        model.addAttribute("books", booksPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", booksPage.getTotalPages());
        model.addAttribute("totalElements", booksPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("title", title);
        model.addAttribute("author", author);
        model.addAttribute("genre", genre);
        
        return "books/list";
    }
    
    @GetMapping("/books/search")
    public String searchBooks(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        
        if (q == null || q.trim().isEmpty()) {
            return "redirect:/books";
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Book> searchResults = bookService.searchBooksByTitle(q.trim(), pageable);
        
        model.addAttribute("books", searchResults);
        model.addAttribute("searchQuery", q);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", searchResults.getTotalPages());
        model.addAttribute("totalElements", searchResults.getTotalElements());
        
        return "books/search-results";
    }
    
    @GetMapping("/about")
    public String about() {
        return "about";
    }
    
    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
    
    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }
    
    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }
    
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
} 