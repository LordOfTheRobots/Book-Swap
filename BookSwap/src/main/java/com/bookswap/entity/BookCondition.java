package com.bookswap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "book_conditions")
public class BookCondition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Min(value = 1, message = "Оценка состояния должна быть от 1 до 5")
    @Max(value = 5, message = "Оценка состояния должна быть от 1 до 5")
    @Column(nullable = false)
    private Integer rating = 5; // От 1 до 5, где 5 - отличное состояние
    
    @Size(max = 1000, message = "Описание состояния не должно превышать 1000 символов")
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "has_damage", nullable = false)
    private Boolean hasDamage = false;
    
    @Size(max = 500, message = "Описание повреждений не должно превышать 500 символов")
    @Column(name = "damage_description", columnDefinition = "TEXT")
    private String damageDescription;
    
    @Column(name = "is_complete", nullable = false)
    private Boolean isComplete = true; // Все страницы на месте
    
    @Column(name = "has_highlighting", nullable = false)
    private Boolean hasHighlighting = false; // Есть ли выделения/подчеркивания
    
    @Column(name = "has_notes", nullable = false)
    private Boolean hasNotes = false; // Есть ли заметки
    
    @Enumerated(EnumType.STRING)
    @Column(name = "cover_condition")
    private CoverCondition coverCondition = CoverCondition.EXCELLENT;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "pages_condition")
    private PagesCondition pagesCondition = PagesCondition.EXCELLENT;
    
    @OneToOne(mappedBy = "condition")
    private Book book;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum CoverCondition {
        EXCELLENT("Отличное"),
        GOOD("Хорошее"),
        FAIR("Удовлетворительное"),
        POOR("Плохое");
        
        private final String displayName;
        
        CoverCondition(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum PagesCondition {
        EXCELLENT("Отличное"),
        GOOD("Хорошее"),
        FAIR("Удовлетворительное"),
        POOR("Плохое");
        
        private final String displayName;
        
        PagesCondition(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public BookCondition() {}
    
    public BookCondition(Integer rating) {
        this.rating = rating;
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
    
    public String getOverallCondition() {
        switch (rating) {
            case 5: return "Отличное состояние";
            case 4: return "Хорошее состояние";
            case 3: return "Удовлетворительное состояние";
            case 2: return "Плохое состояние";
            case 1: return "Очень плохое состояние";
            default: return "Состояние не определено";
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Boolean getHasDamage() { return hasDamage; }
    public void setHasDamage(Boolean hasDamage) { this.hasDamage = hasDamage; }
    
    public String getDamageDescription() { return damageDescription; }
    public void setDamageDescription(String damageDescription) { this.damageDescription = damageDescription; }
    
    public Boolean getIsComplete() { return isComplete; }
    public void setIsComplete(Boolean isComplete) { this.isComplete = isComplete; }
    
    public Boolean getHasHighlighting() { return hasHighlighting; }
    public void setHasHighlighting(Boolean hasHighlighting) { this.hasHighlighting = hasHighlighting; }
    
    public Boolean getHasNotes() { return hasNotes; }
    public void setHasNotes(Boolean hasNotes) { this.hasNotes = hasNotes; }
    
    public CoverCondition getCoverCondition() { return coverCondition; }
    public void setCoverCondition(CoverCondition coverCondition) { this.coverCondition = coverCondition; }
    
    public PagesCondition getPagesCondition() { return pagesCondition; }
    public void setPagesCondition(PagesCondition pagesCondition) { this.pagesCondition = pagesCondition; }
    
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookCondition that = (BookCondition) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "BookCondition{" +
                "id=" + id +
                ", rating=" + rating +
                ", hasDamage=" + hasDamage +
                ", isComplete=" + isComplete +
                '}';
    }
} 