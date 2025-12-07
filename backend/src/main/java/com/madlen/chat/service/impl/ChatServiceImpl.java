package com.madlen.chat.service.impl;

import com.madlen.chat.dto.ChatRequest;
import com.madlen.chat.dto.ChatResponse;
import com.madlen.chat.exception.ResourceNotFoundException;
import com.madlen.chat.model.Conversation;
import com.madlen.chat.model.Message;
import com.madlen.chat.model.User;
import com.madlen.chat.repository.ConversationRepository;
import com.madlen.chat.repository.MessageRepository;
import com.madlen.chat.repository.UserRepository;
import com.madlen.chat.service.ChatService;
import com.madlen.chat.service.ConversationService;
import com.madlen.chat.service.OpenRouterService;
import com.madlen.chat.util.Constants;
import com.madlen.chat.util.ConversationHelper;
import com.madlen.chat.util.MessageFactory;
import com.madlen.chat.util.MessageHistoryBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {
    
    private final OpenRouterService openRouterService;
    private final ConversationService conversationService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final Tracer tracer;
    
    public ChatServiceImpl(OpenRouterService openRouterService,
                          ConversationService conversationService,
                          ConversationRepository conversationRepository,
                          MessageRepository messageRepository,
                          UserRepository userRepository,
                          Tracer tracer) {
        this.openRouterService = openRouterService;
        this.conversationService = conversationService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.tracer = tracer;
    }
    
    @Override
    @Transactional
    public ChatResponse sendMessage(ChatRequest request, Long userId) {
        Span span = tracer.spanBuilder("chat.send_message")
                .setAttribute("user.id", userId)
                .setAttribute("model", request.getModel())
                .setAttribute("has.image", request.getImage() != null && !request.getImage().isEmpty())
                .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            validateUser(userId);
            
            Conversation conversation = getOrCreateConversation(request, userId, span);
            span.setAttribute("conversation.id", conversation.getId());
            
            List<Map<String, String>> history = MessageHistoryBuilder.buildMessageHistory(
                    conversation.getId(), messageRepository);
            span.setAttribute("history.size", history.size());
            
            String response = callOpenRouterAPI(request, history, span);
            
            Message userMessage = saveUserMessage(request, conversation, span);
            Message assistantMessage = saveAssistantMessage(conversation, response, request.getModel(), span);
            
            ConversationHelper.updateConversationTitleIfNeeded(
                    conversation, request.getMessage(), conversationRepository);
            
            span.setAttribute("success", true);
            return new ChatResponse(response, conversation.getId(), assistantMessage.getId());
        } catch (Exception e) {
            span.recordException(e);
            span.setAttribute("success", false);
            throw e;
        } finally {
            span.end();
        }
    }
    
    private void validateUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
    
    private Conversation getOrCreateConversation(ChatRequest request, Long userId, Span parentSpan) {
        Conversation conversation = ConversationHelper.getOrCreateConversation(
                request, userId, conversationRepository, conversationService);
        
        if (request.getConversationId() == null) {
            parentSpan.setAttribute("conversation.created", true);
        }
        
        return conversation;
    }
    
    private String callOpenRouterAPI(ChatRequest request, List<Map<String, String>> history, Span parentSpan) {
        Span apiSpan = tracer.spanBuilder("openrouter.api_call")
                .setAttribute("model", request.getModel())
                .setAttribute("message.length", request.getMessage().length())
                .startSpan();
        
        try {
            String response = openRouterService.sendChatMessage(
                    request.getMessage(),
                    request.getModel(),
                    history,
                    request.getImage()
            );
            apiSpan.setAttribute("response.length", response.length());
            apiSpan.setAttribute("success", true);
            return response;
        } catch (Exception e) {
            apiSpan.recordException(e);
            apiSpan.setAttribute("success", false);
            throw e;
        } finally {
            apiSpan.end();
        }
    }
    
    private Message saveUserMessage(ChatRequest request, Conversation conversation, Span parentSpan) {
        Span saveSpan = tracer.spanBuilder("db.save_message")
                .setAttribute("role", "USER")
                .startSpan();
        
        try {
            Message userMessage = MessageFactory.createUserMessage(
                    conversation,
                    request.getMessage(),
                    request.getModel(),
                    request.getImage()
            );
            userMessage = messageRepository.save(userMessage);
            saveSpan.setAttribute("message.id", userMessage.getId());
            saveSpan.setAttribute("success", true);
            return userMessage;
        } catch (Exception e) {
            saveSpan.recordException(e);
            saveSpan.setAttribute("success", false);
            throw e;
        } finally {
            saveSpan.end();
        }
    }
    
    private Message saveAssistantMessage(Conversation conversation, String response, String model, Span parentSpan) {
        Span saveResponseSpan = tracer.spanBuilder("db.save_message")
                .setAttribute("role", "ASSISTANT")
                .startSpan();
        
        try {
            Message assistantMessage = MessageFactory.createAssistantMessage(conversation, response, model);
            assistantMessage = messageRepository.save(assistantMessage);
            saveResponseSpan.setAttribute("message.id", assistantMessage.getId());
            saveResponseSpan.setAttribute("success", true);
            return assistantMessage;
        } catch (Exception e) {
            saveResponseSpan.recordException(e);
            saveResponseSpan.setAttribute("success", false);
            throw e;
        } finally {
            saveResponseSpan.end();
        }
    }
}

