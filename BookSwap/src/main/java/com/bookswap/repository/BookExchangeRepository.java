package com.bookswap.repository;

import com.bookswap.entity.BookExchange;
import com.bookswap.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookExchangeRepository extends JpaRepository<BookExchange, Long> {
    
    // Spring Data JPA методы
    List<BookExchange> findByRequester(User requester);
    
    List<BookExchange> findByOwner(User owner);
    
    List<BookExchange> findByStatus(BookExchange.ExchangeStatus status);
    
    Page<BookExchange> findByRequesterOrOwner(User requester, User owner, Pageable pageable);
    
    List<BookExchange> findByExchangeType(BookExchange.ExchangeType exchangeType);
    
    // Кастомные запросы
    @Query("SELECT be FROM BookExchange be WHERE be.requester.id = :userId OR be.owner.id = :userId")
    List<BookExchange> findUserExchanges(@Param("userId") Long userId);
    
    @Query("SELECT be FROM BookExchange be WHERE be.status = 'PENDING' AND be.owner.id = :ownerId")
    List<BookExchange> findPendingExchangesForOwner(@Param("ownerId") Long ownerId);
    
    @Query("SELECT be FROM BookExchange be WHERE be.createdAt BETWEEN :startDate AND :endDate")
    List<BookExchange> findExchangesBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(be) FROM BookExchange be WHERE be.status = :status")
    long countByStatus(@Param("status") BookExchange.ExchangeStatus status);
} 