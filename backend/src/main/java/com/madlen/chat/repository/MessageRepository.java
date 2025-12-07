package com.madlen.chat.repository;

import com.madlen.chat.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Message entity için repository interface.
 * 
 * NOT: SQL Injection Güvenliği
 * Bu repository JPA kullanıyor, SQL injection riski yok.
 * Tüm query'ler Spring Data JPA tarafından güvenli şekilde oluşturuluyor.
 * 
 * Güvenlik Kontrolü:
 * JPA method naming convention kullanılıyor
 * Native query kullanılmıyor
 * Parameterized query'ler otomatik oluşturuluyor
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    Page<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId, Pageable pageable);

    long countByConversationId(Long conversationId);
}