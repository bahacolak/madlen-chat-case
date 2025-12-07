package com.madlen.chat.controller;

import com.madlen.chat.dto.ConversationDto;
import com.madlen.chat.service.ConversationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final com.madlen.chat.service.UserService userService;

    public ConversationController(ConversationService conversationService,
            com.madlen.chat.service.UserService userService) {
        this.conversationService = conversationService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ConversationDto> createConversation(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        ConversationDto conversation = conversationService.createConversation(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
    }

    @GetMapping
    public ResponseEntity<Page<ConversationDto>> getUserConversations(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserIdFromAuthentication(authentication);
        Page<ConversationDto> conversations = conversationService.getUserConversations(userId,
                PageRequest.of(page, size));
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationDto> getConversation(@PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        ConversationDto conversation = conversationService.getConversationById(id, userId);
        return ResponseEntity.ok(conversation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        conversationService.deleteConversation(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<Page<ConversationDto.MessageDto>> getConversationMessages(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Long userId = getUserIdFromAuthentication(authentication);
        Page<ConversationDto.MessageDto> messages = conversationService.getConversationMessages(
                id, userId, PageRequest.of(page, size));
        return ResponseEntity.ok(messages);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userService.findByUsername(username).getId();
    }
}
