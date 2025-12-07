package com.madlen.chat.repository;

import com.madlen.chat.model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Conversation entity için repository interface.
 * 
 * NOT: SQL Injection Güvenliği
 * Bu repository JPA kullanıyor, SQL injection riski yok.
 * Tüm query'ler Spring Data JPA tarafından güvenli şekilde oluşturuluyor.
 * 
 * Güvenlik Kontrolü:
 * JPA method naming convention kullanılıyor
 * @EntityGraph ile performans optimizasyonu yapılıyor (güvenli)
 * Native query kullanılmıyor
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @EntityGraph(attributePaths = { "user" })
    Page<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    List<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId);

    @EntityGraph(attributePaths = { "messages", "user" })
    Optional<Conversation> findByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
