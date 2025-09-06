package com.bookswap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "book_exchanges")
public class BookExchange {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Книга обязательна")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @NotNull(message = "Владелец книги обязателен")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner; // Владелец книги
    
    @NotNull(message = "Пользователь, запросивший обмен, обязателен")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester; // Пользователь, который хочет получить книгу
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeStatus status = ExchangeStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "exchange_type", nullable = false)
    private ExchangeType exchangeType = ExchangeType.BOOK_FOR_BOOK;
    
    @Size(max = 1000, message = "Сообщение не должно превышать 1000 символов")
    @Column(columnDefinition = "TEXT")
    private String message; // Сообщение от запросившего
    
    @Column(name = "offered_price", precision = 10, scale = 2)
    private BigDecimal offeredPrice; // Предложенная цена при покупке
    
    @Size(max = 1000, message = "Ответ не должен превышать 1000 символов")
    @Column(name = "owner_response", columnDefinition = "TEXT")
    private String ownerResponse; // Ответ владельца
    
    @Column(name = "exchange_date")
    private LocalDateTime exchangeDate; // Дата проведения обмена
    
    @Size(max = 500, message = "Место встречи не должно превышать 500 символов")
    @Column(name = "meeting_location")
    private String meetingLocation; // Место встречи для обмена
    
    @Column(name = "meeting_date")
    private LocalDateTime meetingDate; // Дата встречи
    
    @Column(name = "is_completed", nullable = false)
    private Boolean completed = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum ExchangeStatus {
        PENDING("Ожидает"),
        ACCEPTED("Принят"),
        REJECTED("Отклонен"),
        COMPLETED("Завершен"),
        CANCELLED("Отменен");
        
        private final String displayName;
        
        ExchangeStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ExchangeType {
        BOOK_FOR_BOOK("Книга за книгу"),
        BOOK_FOR_MONEY("Продажа книги"),
        FREE_GIFT("Бесплатно");
        
        private final String displayName;
        
        ExchangeType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public BookExchange() {}
    
    public BookExchange(Book book, User owner, User requester, ExchangeType exchangeType) {
        this.book = book;
        this.owner = owner;
        this.requester = requester;
        this.exchangeType = exchangeType;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public boolean isPending() {
        return status == ExchangeStatus.PENDING;
    }
    
    public boolean isAccepted() {
        return status == ExchangeStatus.ACCEPTED;
    }
    
    public boolean isCompleted() {
        return status == ExchangeStatus.COMPLETED;
    }
    
    public boolean canBeAccepted() {
        return status == ExchangeStatus.PENDING;
    }
    
    public boolean canBeCancelled() {
        return status == ExchangeStatus.PENDING || status == ExchangeStatus.ACCEPTED;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    
    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }
    
    public ExchangeStatus getStatus() { return status; }
    public void setStatus(ExchangeStatus status) { this.status = status; }
    
    public ExchangeType getExchangeType() { return exchangeType; }
    public void setExchangeType(ExchangeType exchangeType) { this.exchangeType = exchangeType; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public BigDecimal getOfferedPrice() { return offeredPrice; }
    public void setOfferedPrice(BigDecimal offeredPrice) { this.offeredPrice = offeredPrice; }
    
    public String getOwnerResponse() { return ownerResponse; }
    public void setOwnerResponse(String ownerResponse) { this.ownerResponse = ownerResponse; }
    
    public LocalDateTime getExchangeDate() { return exchangeDate; }
    public void setExchangeDate(LocalDateTime exchangeDate) { this.exchangeDate = exchangeDate; }
    
    public String getMeetingLocation() { return meetingLocation; }
    public void setMeetingLocation(String meetingLocation) { this.meetingLocation = meetingLocation; }
    
    public LocalDateTime getMeetingDate() { return meetingDate; }
    public void setMeetingDate(LocalDateTime meetingDate) { this.meetingDate = meetingDate; }
    
    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookExchange that = (BookExchange) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "BookExchange{" +
                "id=" + id +
                ", book=" + (book != null ? book.getTitle() : null) +
                ", status=" + status +
                ", exchangeType=" + exchangeType +
                ", completed=" + completed +
                '}';
    }
} 