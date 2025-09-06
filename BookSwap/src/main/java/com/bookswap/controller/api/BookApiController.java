package com.bookswap.controller.api;

import com.bookswap.entity.Book;
import com.bookswap.entity.User;
import com.bookswap.service.BookService;
import com.bookswap.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "API для управления книгами")
public class BookApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookApiController.class);
    
    private final BookService bookService;
    private final UserService userService;
    
    @Autowired
    public BookApiController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }
    
    @Operation(summary = "Получить все книги", description = "Возвращает постраничный список всех книг")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешно получен список книг"),
        @ApiResponse(responseCode = "400", description = "Неверные параметры запроса")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBooks(
            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Направление сортировки") @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Book> booksPage = bookService.findAll(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("books", booksPage.getContent());
            response.put("currentPage", booksPage.getNumber());
            response.put("totalItems", booksPage.getTotalElements());
            response.put("totalPages", booksPage.getTotalPages());
            response.put("hasNext", booksPage.hasNext());
            response.put("hasPrevious", booksPage.hasPrevious());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка при получении списка книг", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при получении списка книг");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @Operation(summary = "Получить книгу по ID", description = "Возвращает информацию о книге по её идентификатору")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Книга найдена"),
        @ApiResponse(responseCode = "404", description = "Книга не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBookById(
            @Parameter(description = "ID книги") @PathVariable Long id) {
        
        try {
            Optional<Book> bookOpt = bookService.findById(id);
            
            if (bookOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("book", bookOpt.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Книга не найдена");
                errorResponse.put("message", "Книга с ID " + id + " не существует");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
        } catch (Exception e) {
            logger.error("Ошибка при получении книги с ID: " + id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при получении книги");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @Operation(summary = "Создать новую книгу", description = "Создаёт новую книгу для текущего пользователя")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Книга успешно создана"),
        @ApiResponse(responseCode = "400", description = "Неверные данные книги"),
        @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> createBook(
            @Valid @RequestBody Book book,
            Authentication authentication) {
        
        try {
            // Получаем текущего пользователя
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Пользователь не найден");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            book.setOwner(userOpt.get());
            Book savedBook = bookService.createBook(book);
            
            Map<String, Object> response = new HashMap<>();
            response.put("book", savedBook);
            response.put("message", "Книга успешно создана");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Ошибка при создании книги", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при создании книги");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @Operation(summary = "Обновить книгу", description = "Обновляет информацию о книге")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Книга успешно обновлена"),
        @ApiResponse(responseCode = "400", description = "Неверные данные книги"),
        @ApiResponse(responseCode = "403", description = "Нет прав для редактирования книги"),
        @ApiResponse(responseCode = "404", description = "Книга не найдена")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> updateBook(
            @Parameter(description = "ID книги") @PathVariable Long id,
            @Valid @RequestBody Book book,
            Authentication authentication) {
        
        try {
            Optional<Book> existingBookOpt = bookService.findById(id);
            if (existingBookOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Книга не найдена");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Book existingBook = existingBookOpt.get();
            
            // Проверяем, что пользователь является владельцем книги или администратором
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isEmpty() || 
                (!existingBook.getOwner().getId().equals(userOpt.get().getId()) && 
                 !userOpt.get().getRole().equals(User.Role.ADMIN))) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Нет прав для редактирования этой книги");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            
            book.setId(id);
            book.setOwner(existingBook.getOwner());
            Book updatedBook = bookService.updateBook(book);
            
            Map<String, Object> response = new HashMap<>();
            response.put("book", updatedBook);
            response.put("message", "Книга успешно обновлена");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка при обновлении книги с ID: " + id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при обновлении книги");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @Operation(summary = "Удалить книгу", description = "Удаляет книгу по её идентификатору")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Книга успешно удалена"),
        @ApiResponse(responseCode = "403", description = "Нет прав для удаления книги"),
        @ApiResponse(responseCode = "404", description = "Книга не найдена")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> deleteBook(
            @Parameter(description = "ID книги") @PathVariable Long id,
            Authentication authentication) {
        
        try {
            Optional<Book> bookOpt = bookService.findById(id);
            if (bookOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Книга не найдена");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Book book = bookOpt.get();
            
            // Проверяем права доступа
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isEmpty() || 
                (!book.getOwner().getId().equals(userOpt.get().getId()) && 
                 !userOpt.get().getRole().equals(User.Role.ADMIN))) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Нет прав для удаления этой книги");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            
            bookService.deleteBook(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Книга успешно удалена");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка при удалении книги с ID: " + id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при удалении книги");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @Operation(summary = "Поиск книг", description = "Поиск книг по различным критериям")
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchBooks(
            @Parameter(description = "Название книги") @RequestParam(required = false) String title,
            @Parameter(description = "Автор книги") @RequestParam(required = false) String author,
            @Parameter(description = "Жанр книги") @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Book> searchResults = bookService.searchBooks(title, author, genre, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("books", searchResults.getContent());
            response.put("currentPage", searchResults.getNumber());
            response.put("totalItems", searchResults.getTotalElements());
            response.put("totalPages", searchResults.getTotalPages());
            response.put("searchCriteria", Map.of(
                "title", title != null ? title : "",
                "author", author != null ? author : "",
                "genre", genre != null ? genre : ""
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка при поиске книг", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при поиске книг");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @Operation(summary = "Получить доступные книги", description = "Возвращает список книг, доступных для обмена")
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        try {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            Long userId = userOpt.map(User::getId).orElse(null);
            
            List<Book> availableBooks = userId != null ? 
                    bookService.findAvailableBooksForUser(userId) :
                    bookService.findBooksByStatus(Book.ExchangeStatus.AVAILABLE);
            
            // Простая пагинация для списка
            int start = page * size;
            int end = Math.min(start + size, availableBooks.size());
            List<Book> pageContent = start < availableBooks.size() ? 
                    availableBooks.subList(start, end) : List.of();
            
            Map<String, Object> response = new HashMap<>();
            response.put("books", pageContent);
            response.put("currentPage", page);
            response.put("totalItems", availableBooks.size());
            response.put("totalPages", (int) Math.ceil((double) availableBooks.size() / size));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Ошибка при получении доступных книг", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при получении доступных книг");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 