package com.farmmarket.service;

import com.farmmarket.entity.Conversation;
import com.farmmarket.entity.Message;
import com.farmmarket.entity.User;
import com.farmmarket.exception.BadRequestException;
import com.farmmarket.exception.ResourceNotFoundException;
import com.farmmarket.repository.ConversationRepository;
import com.farmmarket.repository.MessageRepository;
import com.farmmarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public Page<Conversation> getUserConversations(UUID userId, int page) {
        return conversationRepository.findByUserId(
                userId, PageRequest.of(page, 20));
    }

    @Transactional
    public Conversation getOrCreateConversation(
            UUID consumerId, UUID farmerId) {
        return conversationRepository
                .findByFarmerIdAndConsumerId(farmerId, consumerId)
                .orElseGet(() -> {
                    User consumer = userRepository.getReferenceById(consumerId);
                    User farmer = userRepository.getReferenceById(farmerId);
                    Conversation conv = Conversation.builder()
                            .consumer(consumer)
                            .farmer(farmer)
                            .build();
                    return conversationRepository.save(conv);
                });
    }

    public Page<Message> getMessages(UUID conversationId, UUID userId, int page) {
        // Mark messages as read
        messageRepository.markConversationAsRead(conversationId, userId);

        return messageRepository.findByConversationIdOrderByCreatedAtDesc(
                conversationId, PageRequest.of(page, 50));
    }

    @Transactional
    public Message sendMessage(UUID conversationId, UUID senderId, String content) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found"));

        // Verify sender is part of conversation
        boolean isFarmer = conv.getFarmer().getId().equals(senderId);
        boolean isConsumer = conv.getConsumer().getId().equals(senderId);
        if (!isFarmer && !isConsumer) {
            throw new BadRequestException("Not part of this conversation");
        }

        User sender = userRepository.getReferenceById(senderId);

        Message message = Message.builder()
                .conversation(conv)
                .sender(sender)
                .content(content)
                .build();

        message = messageRepository.save(message);

        conv.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conv);

        // Push via WebSocket to the other user
        UUID recipientId = isFarmer
                ? conv.getConsumer().getId()
                : conv.getFarmer().getId();

        messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/messages",
                Map.of(
                        "conversationId", conversationId,
                        "senderId", senderId,
                        "content", content,
                        "timestamp", message.getCreatedAt().toString()
                )
        );

        return message;
    }
}