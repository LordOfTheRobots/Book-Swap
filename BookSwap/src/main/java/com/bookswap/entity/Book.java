package com.bookswap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "books")
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Название книги обязательно")
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    @Column(nullable = false)
    private String title;
    
    @Size(max = 20, message = "ISBN не должен превышать 20 символов")
    @Column(unique = true, length = 20)
    private String isbn;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "cover_image_url")
    private String coverImageUrl;
    
    @Column(name = "publication_year")
    private Integer publicationYear;
    
    @Size(max = 100, message = "Издательство не должно превышать 100 символов")
    @Column(length = 100)
    private String publisher;
    
    @Column(name = "page_count")
    @Positive(message = "Количество страниц должно быть положительным")
    private Integer pageCount;
    
    @Size(max = 50, message = "Язык не должен превышать 50 символов")
    @Column(length = 50)
    private String language = "Русский";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "exchange_status", nullable = false)
    private ExchangeStatus exchangeStatus = ExchangeStatus.AVAILABLE;
    
    @Column(name = "estimated_price", precision = 10, scale = 2)
    private BigDecimal estimatedPrice;
    
    @NotNull(message = "Владелец книги обязателен")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_id")
    private BookCondition condition;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_authors",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_genres",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Review> reviews = new HashSet<>();
    
    @OneToMany(mappedBy = "book", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<BookExchange> exchanges = new HashSet<>();
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum ExchangeStatus {
        AVAILABLE("Доступна для обмена"),
        RESERVED("Зарезервирована"),
        EXCHANGED("Обменена"),
        NOT_AVAILABLE("Недоступна");
        
        private final String displayName;
        
        ExchangeStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public Book() {}
    
    public Book(String title, User owner) {
        this.title = title;
        this.owner = owner;
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
    
    // Helper methods
    public void addAuthor(Author author) {
        authors.add(author);
        author.getBooks().add(this);
    }
    
    public void removeAuthor(Author author) {
        authors.remove(author);
        author.getBooks().remove(this);
    }
    
    public void addGenre(Genre genre) {
        genres.add(genre);
        genre.getBooks().add(this);
    }
    
    public void removeGenre(Genre genre) {
        genres.remove(genre);
        genre.getBooks().remove(this);
    }
    
    public String getAuthorsAsString() {
        return authors.stream()
                .map(author -> author.getFirstName() + " " + author.getLastName())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Автор неизвестен");
    }
    
    public String getGenresAsString() {
        return genres.stream()
                .map(Genre::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Жанр не указан");
    }
    
    public double getAverageRating() {
        return reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    
    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }
    
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public ExchangeStatus getExchangeStatus() { return exchangeStatus; }
    public void setExchangeStatus(ExchangeStatus exchangeStatus) { this.exchangeStatus = exchangeStatus; }
    
    public BigDecimal getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(BigDecimal estimatedPrice) { this.estimatedPrice = estimatedPrice; }
    
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    
    public BookCondition getCondition() { return condition; }
    public void setCondition(BookCondition condition) { this.condition = condition; }
    
    public Set<Author> getAuthors() { return authors; }
    public void setAuthors(Set<Author> authors) { this.authors = authors; }
    
    public Set<Genre> getGenres() { return genres; }
    public void setGenres(Set<Genre> genres) { this.genres = genres; }
    
    public Set<Review> getReviews() { return reviews; }
    public void setReviews(Set<Review> reviews) { this.reviews = reviews; }
    
    public Set<BookExchange> getExchanges() { return exchanges; }
    public void setExchanges(Set<BookExchange> exchanges) { this.exchanges = exchanges; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id) && Objects.equals(title, book.title);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }
    
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isbn='" + isbn + '\'' +
                ", exchangeStatus=" + exchangeStatus +
                '}';
    }
} 