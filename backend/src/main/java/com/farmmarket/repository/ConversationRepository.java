package com.farmmarket.repository;

import com.farmmarket.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("SELECT c FROM Conversation c " +
            "WHERE (c.farmer.id = :userId OR c.consumer.id = :userId) " +
            "ORDER BY c.lastMessageAt DESC NULLS LAST")
    Page<Conversation> findByUserId(
            @Param("userId") UUID userId, Pageable pageable);

    Optional<Conversation> findByFarmerIdAndConsumerId(
            UUID farmerId, UUID consumerId);
}