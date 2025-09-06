package com.bookswap.repository;

import com.bookswap.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    
    List<Author> findByLastNameContainingIgnoreCase(String lastName);
    
    Optional<Author> findByFirstNameAndLastName(String firstName, String lastName);
    
    List<Author> findByNationality(String nationality);
    
    @Query("SELECT a FROM Author a WHERE SIZE(a.books) > :bookCount")
    List<Author> findAuthorsWithMoreThanBooks(@Param("bookCount") int bookCount);
    
    @Query("SELECT a FROM Author a ORDER BY SIZE(a.books) DESC")
    List<Author> findAuthorsByPopularity();
} 