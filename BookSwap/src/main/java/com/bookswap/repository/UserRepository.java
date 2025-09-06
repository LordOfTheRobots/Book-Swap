package com.bookswap.repository;

import com.bookswap.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Spring Data JPA методы
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(User.Role role);
    
    List<User> findByEnabledTrue();
    
    Page<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);
    
    // Кастомные запросы с @Query
    @Query("SELECT u FROM User u WHERE u.createdAt >= :date")
    List<User> findUsersRegisteredAfter(@Param("date") LocalDateTime date);
    
    @Query("SELECT u FROM User u WHERE SIZE(u.ownedBooks) > :bookCount")
    List<User> findUsersWithMoreThanBooks(@Param("bookCount") int bookCount);
    
    @Query(value = "SELECT * FROM users u WHERE u.created_at >= ?1 AND " +
           "EXISTS (SELECT 1 FROM books b WHERE b.owner_id = u.id)", 
           nativeQuery = true)
    List<User> findActiveUsersWithBooks(LocalDateTime date);
    
    @Query("SELECT u FROM User u WHERE u.id IN " +
           "(SELECT DISTINCT be.owner.id FROM BookExchange be WHERE be.status = 'COMPLETED')")
    List<User> findUsersWithCompletedExchanges();
    
    @Modifying
    @Query("UPDATE User u SET u.enabled = false WHERE u.updatedAt < :date")
    int deactivateInactiveUsers(@Param("date") LocalDateTime date);
    
    // Запрос с подзапросом - пользователи, у которых есть книги в определенном жанре
    @Query("SELECT DISTINCT u FROM User u WHERE u.id IN " +
           "(SELECT b.owner.id FROM Book b JOIN b.genres g WHERE g.name = :genreName)")
    List<User> findUsersByBookGenre(@Param("genreName") String genreName);
    
    // Статистические запросы
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.Role role);
    
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> getUserStatsByRole();
} 