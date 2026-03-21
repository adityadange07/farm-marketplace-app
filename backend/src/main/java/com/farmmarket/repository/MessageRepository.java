package com.farmmarket.repository;

import com.farmmarket.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByConversationIdOrderByCreatedAtDesc(
            UUID conversationId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = true " +
            "WHERE m.conversation.id = :convId " +
            "AND m.sender.id != :userId AND m.isRead = false")
    void markConversationAsRead(
            @Param("convId") UUID conversationId,
            @Param("userId") UUID userId);
}