package com.madlen.chat.repository;

import com.madlen.chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User entity için repository interface.
 * 
 * NOT: SQL Injection Güvenliği
 * Bu repository JPA (Java Persistence API) kullanıyor, bu nedenle SQL injection riski yok.
 * Spring Data JPA otomatik olarak:
 * - Tüm query'leri parameterized statement'lara çevirir
 * - Method name'lerden güvenli JPQL query'leri oluşturur
 * - Native query kullanılmadığı sürece SQL injection mümkün değildir
 * 
 * Güvenlik Kontrolü:
 * Tüm method'lar JPA method naming convention kullanıyor
 * Native @Query annotation'ı kullanılmıyor
 * Parameterized query'ler otomatik oluşturuluyor
 * 
 * Eğer gelecekte native query kullanılacaksa:
 * - Mutlaka parameterized query kullanılmalı: @Query(value = "SELECT * FROM users WHERE username = ?1", nativeQuery = true)
 * - Veya named parameter kullanılmalı: @Query(value = "SELECT * FROM users WHERE username = :username", nativeQuery = true)
 * - String concatenation ASLA kullanılmamalı
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}

