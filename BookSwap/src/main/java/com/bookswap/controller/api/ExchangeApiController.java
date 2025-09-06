package com.bookswap.controller.api;

import com.bookswap.entity.BookExchange;
import com.bookswap.service.ExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API контроллер для управления обменами книг
 * Делегирует всю бизнес-логику в ExchangeService
 */
@RestController
@RequestMapping("/api/exchanges")
@Tag(name = "Exchange Management", description = "API для управления обменами книг")
public class ExchangeApiController {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeApiController.class);

    private final ExchangeService exchangeService;

    @Autowired
    public ExchangeApiController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    /**
     * Создание запроса на обмен
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Создать запрос на обмен", description = "Создает новый запрос на обмен книги")
    public ResponseEntity<Map<String, Object>> createExchangeRequest(
            @Parameter(description = "ID книги для обмена") @RequestParam Long bookId,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            BookExchange exchange = exchangeService.createExchangeRequest(bookId, username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Exchange request created successfully");
            response.put("exchangeId", exchange.getId());

            logger.info("Exchange request created: {} for book: {}", exchange.getId(), bookId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error creating exchange request for book {}: {}", bookId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Одобрение запроса на обмен
     */
    @PutMapping("/{exchangeId}/approve")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Одобрить запрос на обмен", description = "Одобряет запрос на обмен книги")
    public ResponseEntity<Map<String, Object>> approveExchange(
            @Parameter(description = "ID обмена") @PathVariable Long exchangeId,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            BookExchange exchange = exchangeService.approveExchange(exchangeId, username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Exchange approved successfully");
            response.put("exchange", exchange);

            logger.info("Exchange {} approved by user {}", exchangeId, username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error approving exchange {}: {}", exchangeId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Завершение обмена
     */
    @PutMapping("/{exchangeId}/complete")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Завершить обмен", description = "Завершает процесс обмена книги")
    public ResponseEntity<Map<String, Object>> completeExchange(
            @Parameter(description = "ID обмена") @PathVariable Long exchangeId,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            BookExchange exchange = exchangeService.completeExchange(exchangeId, username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Exchange completed successfully");
            response.put("exchange", exchange);

            logger.info("Exchange {} completed by user {}", exchangeId, username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error completing exchange {}: {}", exchangeId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Отклонение запроса на обмен
     */
    @PutMapping("/{exchangeId}/reject")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Отклонить запрос на обмен", description = "Отклоняет запрос на обмен книги")
    public ResponseEntity<Map<String, Object>> rejectExchange(
            @Parameter(description = "ID обмена") @PathVariable Long exchangeId,
            @Parameter(description = "Причина отклонения") @RequestParam(required = false) String reason,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            exchangeService.rejectExchange(exchangeId, username, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Exchange rejected successfully");

            logger.info("Exchange {} rejected by user {}", exchangeId, username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error rejecting exchange {}: {}", exchangeId, e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Получение обменов пользователя
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Получить мои обмены", description = "Возвращает все обмены текущего пользователя")
    public ResponseEntity<Page<BookExchange>> getUserExchanges(
            Authentication authentication,
            Pageable pageable) {

        try {
            String username = authentication.getName();
            Page<BookExchange> exchanges = exchangeService.getUserExchanges(username, pageable);

            logger.debug("Retrieved {} exchanges for user {}", exchanges.getTotalElements(), username);
            return ResponseEntity.ok(exchanges);

        } catch (Exception e) {
            logger.error("Error retrieving exchanges for user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение входящих запросов
     */
    @GetMapping("/incoming")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Получить входящие запросы", description = "Возвращает входящие запросы на обмен")
    public ResponseEntity<List<BookExchange>> getIncomingRequests(Authentication authentication) {

        try {
            String username = authentication.getName();
            List<BookExchange> requests = exchangeService.getIncomingRequests(username);

            logger.debug("Retrieved {} incoming requests for user {}", requests.size(), username);
            return ResponseEntity.ok(requests);

        } catch (Exception e) {
            logger.error("Error retrieving incoming requests: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение исходящих запросов
     */
    @GetMapping("/outgoing")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Получить исходящие запросы", description = "Возвращает исходящие запросы на обмен")
    public ResponseEntity<List<BookExchange>> getOutgoingRequests(Authentication authentication) {

        try {
            String username = authentication.getName();
            List<BookExchange> requests = exchangeService.getOutgoingRequests(username);

            logger.debug("Retrieved {} outgoing requests for user {}", requests.size(), username);
            return ResponseEntity.ok(requests);

        } catch (Exception e) {
            logger.error("Error retrieving outgoing requests: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение конкретного обмена
     */
    @GetMapping("/{exchangeId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Получить обмен по ID", description = "Возвращает информацию о конкретном обмене")
    public ResponseEntity<BookExchange> getExchange(
            @Parameter(description = "ID обмена") @PathVariable Long exchangeId,
            Authentication authentication) {

        try {
            // Здесь можно добавить проверку доступа к обмену
            // Пока что просто пример
            logger.debug("Retrieving exchange {}", exchangeId);
            
            // Заглушка - в реальном приложении нужно реализовать метод в сервисе
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error retrieving exchange {}: {}", exchangeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 