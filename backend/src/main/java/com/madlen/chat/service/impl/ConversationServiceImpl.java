package com.madlen.chat.service.impl;

import com.madlen.chat.dto.ConversationDto;
import com.madlen.chat.exception.ResourceNotFoundException;
import com.madlen.chat.model.Conversation;
import com.madlen.chat.model.Message;
import com.madlen.chat.model.User;
import com.madlen.chat.repository.ConversationRepository;
import com.madlen.chat.repository.UserRepository;
import com.madlen.chat.service.ConversationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final com.madlen.chat.repository.MessageRepository messageRepository;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
            UserRepository userRepository,
            com.madlen.chat.repository.MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public ConversationDto createConversation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Conversation conversation = new Conversation();
        conversation.setUser(user);
        conversation.setTitle(com.madlen.chat.util.Constants.DEFAULT_CONVERSATION_TITLE);
        conversation = conversationRepository.save(conversation);

        return convertToDto(conversation);
    }

    @Override
    public List<ConversationDto> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        return conversations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ConversationDto> getUserConversations(Long userId, Pageable pageable) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable)
                .map(this::convertToDto);
    }

    @Override
    public ConversationDto getConversationById(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));

        return convertToDto(conversation);
    }

    @Override
    @Transactional
    public void deleteConversation(Long conversationId, Long userId) {
        conversationRepository.deleteByIdAndUserId(conversationId, userId);
    }

    private ConversationDto convertToDto(Conversation conversation) {
        ConversationDto dto = new ConversationDto();
        dto.setId(conversation.getId());
        dto.setTitle(conversation.getTitle());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setUpdatedAt(conversation.getUpdatedAt());

        if (conversation.getMessages() != null) {
            List<ConversationDto.MessageDto> messageDtos = conversation.getMessages().stream()
                    .map(this::convertMessageToDto)
                    .collect(Collectors.toList());
            dto.setMessages(messageDtos);
        }

        return dto;
    }

    private ConversationDto.MessageDto convertMessageToDto(Message message) {
        ConversationDto.MessageDto dto = new ConversationDto.MessageDto();
        dto.setId(message.getId());
        dto.setRole(message.getRole().name());
        dto.setContent(message.getContent());
        dto.setModel(message.getModel());
        dto.setImageUrl(message.getImageUrl());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }

    @Override
    public Page<ConversationDto.MessageDto> getConversationMessages(Long conversationId, Long userId,
            Pageable pageable) {
        conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable)
                .map(this::convertMessageToDto);
    }
}
