package com.bookswap.repository;

import com.bookswap.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    
    Optional<Genre> findByName(String name);
    
    List<Genre> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT g FROM Genre g ORDER BY SIZE(g.books) DESC")
    List<Genre> findGenresByPopularity();
} 