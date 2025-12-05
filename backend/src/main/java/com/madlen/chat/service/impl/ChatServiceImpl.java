package com.madlen.chat.service.impl;

import com.madlen.chat.dto.ChatRequest;
import com.madlen.chat.dto.ChatResponse;
import com.madlen.chat.dto.ConversationDto;
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
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
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
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));
            
            Conversation conversation;
            if (request.getConversationId() != null) {
                Span dbSpan = tracer.spanBuilder("db.get_conversation")
                        .setAttribute("conversation.id", request.getConversationId())
                        .startSpan();
                try {
                    conversation = conversationRepository.findByIdAndUserId(request.getConversationId(), userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Conversation", request.getConversationId()));
                    dbSpan.setAttribute("success", true);
                } catch (Exception e) {
                    dbSpan.recordException(e);
                    dbSpan.setAttribute("success", false);
                    throw e;
                } finally {
                    dbSpan.end();
                }
            } else {
                ConversationDto newConv = conversationService.createConversation(userId);
                conversation = conversationRepository.findById(newConv.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Conversation", newConv.getId()));
                span.setAttribute("conversation.created", true);
            }
            
            span.setAttribute("conversation.id", conversation.getId());
            
            // Get conversation history
            List<Map<String, String>> history = buildMessageHistory(conversation.getId());
            span.setAttribute("history.size", history.size());
            
            // Call OpenRouter API
            Span apiSpan = tracer.spanBuilder("openrouter.api_call")
                    .setAttribute("model", request.getModel())
                    .setAttribute("message.length", request.getMessage().length())
                    .startSpan();
            String response;
            try {
                response = openRouterService.sendChatMessage(
                        request.getMessage(),
                        request.getModel(),
                        history,
                        request.getImage()
                );
                apiSpan.setAttribute("response.length", response.length());
                apiSpan.setAttribute("success", true);
            } catch (Exception e) {
                apiSpan.recordException(e);
                apiSpan.setAttribute("success", false);
                throw e;
            } finally {
                apiSpan.end();
            }
            
            // Save user message
            Span saveSpan = tracer.spanBuilder("db.save_message")
                    .setAttribute("role", "USER")
                    .startSpan();
            Message userMessage;
            try {
                userMessage = new Message();
                userMessage.setConversation(conversation);
                userMessage.setRole(Message.MessageRole.USER);
                userMessage.setContent(request.getMessage());
                userMessage.setModel(request.getModel());
                if (request.getImage() != null && !request.getImage().isEmpty()) {
                    userMessage.setImageUrl("data:image/jpeg;base64," + request.getImage());
                }
                userMessage = messageRepository.save(userMessage);
                saveSpan.setAttribute("message.id", userMessage.getId());
                saveSpan.setAttribute("success", true);
            } catch (Exception e) {
                saveSpan.recordException(e);
                saveSpan.setAttribute("success", false);
                throw e;
            } finally {
                saveSpan.end();
            }
            
            // Save assistant response
            Span saveResponseSpan = tracer.spanBuilder("db.save_message")
                    .setAttribute("role", "ASSISTANT")
                    .startSpan();
            Message assistantMessage;
            try {
                assistantMessage = new Message();
                assistantMessage.setConversation(conversation);
                assistantMessage.setRole(Message.MessageRole.ASSISTANT);
                assistantMessage.setContent(response);
                assistantMessage.setModel(request.getModel());
                assistantMessage = messageRepository.save(assistantMessage);
                saveResponseSpan.setAttribute("message.id", assistantMessage.getId());
                saveResponseSpan.setAttribute("success", true);
            } catch (Exception e) {
                saveResponseSpan.recordException(e);
                saveResponseSpan.setAttribute("success", false);
                throw e;
            } finally {
                saveResponseSpan.end();
            }
            
            // Update conversation title if it's the first message
            List<Message> conversationMessages = conversation.getMessages();
            if (conversation.getTitle().equals("New Conversation") && 
                (conversationMessages == null || conversationMessages.size() <= 2)) {
                String title = request.getMessage().length() > 50 
                        ? request.getMessage().substring(0, 50) + "..."
                        : request.getMessage();
                conversation.setTitle(title);
                conversationRepository.save(conversation);
            }
            
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
    
    private List<Map<String, String>> buildMessageHistory(Long conversationId) {
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        List<Map<String, String>> history = new ArrayList<>();
        
        for (Message message : messages) {
            Map<String, String> msgMap = new HashMap<>();
            msgMap.put("role", message.getRole().name().toLowerCase());
            msgMap.put("content", message.getContent());
            history.add(msgMap);
        }
        
        return history;
    }
}

