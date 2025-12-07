package com.madlen.chat.service.impl;

import com.madlen.chat.dto.ChatRequest;
import com.madlen.chat.exception.ResourceNotFoundException;
import com.madlen.chat.model.Conversation;
import com.madlen.chat.model.Message;
import com.madlen.chat.repository.ConversationRepository;
import com.madlen.chat.repository.MessageRepository;
import com.madlen.chat.service.ConversationService;
import com.madlen.chat.service.OpenRouterService;
import com.madlen.chat.service.StreamingChatService;
import com.madlen.chat.util.Constants;
import com.madlen.chat.util.ConversationHelper;
import com.madlen.chat.util.MessageFactory;
import com.madlen.chat.util.MessageHistoryBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class StreamingChatServiceImpl implements StreamingChatService {

    private final OpenRouterService openRouterService;
    private final ConversationService conversationService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final Tracer tracer;

    public StreamingChatServiceImpl(OpenRouterService openRouterService,
            ConversationService conversationService,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            Tracer tracer) {
        this.openRouterService = openRouterService;
        this.conversationService = conversationService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.tracer = tracer;
    }

    @Override
    public Flux<ServerSentEvent<String>> streamChat(ChatRequest request, Long userId) {
        Span span = tracer.spanBuilder("streaming.chat")
                .setAttribute("model", request.getModel())
                .setAttribute("userId", userId)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            Conversation conversation = getOrCreateConversation(request, userId);
            final Long conversationId = conversation.getId();
            span.setAttribute("conversationId", conversationId);

            saveUserMessage(request, conversation);

            List<Map<String, String>> history = MessageHistoryBuilder.buildMessageHistory(
                    conversationId, messageRepository);
            span.setAttribute("historySize", history.size());

            StringBuilder fullResponse = new StringBuilder();
            AtomicReference<Long> messageIdRef = new AtomicReference<>();

            Flux<ServerSentEvent<String>> initEvent = Flux.just(
                    ServerSentEvent.<String>builder()
                            .event("init")
                            .data("{\"conversationId\":" + conversationId + "}")
                            .build());

            Flux<ServerSentEvent<String>> contentStream = createContentStream(
                    request, history, conversationId, fullResponse, messageIdRef);

            Flux<ServerSentEvent<String>> completeEvent = Mono.defer(() -> Mono.just(ServerSentEvent.<String>builder()
                    .event("complete")
                    .data("{\"messageId\":" + (messageIdRef.get() != null ? messageIdRef.get() : 0)
                            + ",\"conversationId\":"
                            + conversationId + "}")
                    .build())).flux();

            return initEvent.concatWith(contentStream).concatWith(completeEvent)
                    .doOnComplete(() -> {
                        span.setStatus(StatusCode.OK);
                        span.end();
                    })
                    .doOnError(error -> {
                        span.setStatus(StatusCode.ERROR, error.getMessage());
                        span.recordException(error);
                        span.end();
                    });
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            span.end();
            throw e;
        }
    }

    private Conversation getOrCreateConversation(ChatRequest request, Long userId) {
        return ConversationHelper.getOrCreateConversation(
                request, userId, conversationRepository, conversationService);
    }

    private void saveUserMessage(ChatRequest request, Conversation conversation) {
        Message userMessage = MessageFactory.createUserMessage(
                conversation,
                request.getMessage(),
                request.getModel(),
                request.getImage()
        );
        messageRepository.save(userMessage);
    }

    private Flux<ServerSentEvent<String>> createContentStream(
            ChatRequest request,
            List<Map<String, String>> history,
            Long conversationId,
            StringBuilder fullResponse,
            AtomicReference<Long> messageIdRef) {

        boolean testMode = request.getMessage() != null && request.getMessage().startsWith(Constants.TEST_MESSAGE_PREFIX);
        String testMessage = testMode 
                ? request.getMessage().substring(Constants.TEST_MESSAGE_PREFIX.length()) 
                : null;

        if (testMode) {
            return createTestStream(testMessage, request.getModel(), conversationId, fullResponse, messageIdRef);
        } else {
            return createRealStream(request, history, conversationId, fullResponse, messageIdRef);
        }
    }

    private Flux<ServerSentEvent<String>> createTestStream(
            String testMessage,
            String model,
            Long conversationId,
            StringBuilder fullResponse,
            AtomicReference<Long> messageIdRef) {

        String mockResponse = "This is a test streaming response. Your message: \"" + testMessage + "\"\n\n" +
                "Streaming feature is working! Messages are coming word by word. ";
        String[] words = mockResponse.split(" ");

        return Flux.fromIterable(Arrays.asList(words))
                .delayElements(Duration.ofMillis(100), Schedulers.boundedElastic())
                .map(word -> {
                    fullResponse.append(word).append(" ");
                    return ServerSentEvent.<String>builder()
                            .event("content")
                            .data(word + " ")
                            .build();
                })
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .event("content")
                        .data("\n\n✅ Streaming successfully tested!")
                        .build()))
                .doOnComplete(() -> {
                    fullResponse.append("\n\n✅ Streaming successfully tested!");
                    saveAssistantMessageAndUpdateTitle(conversationId, fullResponse.toString(), model, testMessage,
                            messageIdRef);
                });
    }

    private Flux<ServerSentEvent<String>> createRealStream(
            ChatRequest request,
            List<Map<String, String>> history,
            Long conversationId,
            StringBuilder fullResponse,
            AtomicReference<Long> messageIdRef) {

        return openRouterService.streamChatMessage(
                request.getMessage(),
                request.getModel(),
                history,
                request.getImage())
                .doOnNext(chunk -> fullResponse.append(chunk))
                .map(chunk -> ServerSentEvent.<String>builder()
                        .event("content")
                        .data(chunk)
                        .build())
                .doOnComplete(() -> {
                    saveAssistantMessageAndUpdateTitle(conversationId, fullResponse.toString(), request.getModel(),
                            request.getMessage(), messageIdRef);
                })
                .onErrorResume(error -> {
                    String errorMessage = error.getMessage();
                    if (errorMessage != null
                            && (errorMessage.contains("429") || errorMessage.toLowerCase().contains("rate limit")
                                    || errorMessage.toLowerCase().contains("too many requests"))) {
                        errorMessage = "429 Too Many Requests: Rate limit exceeded. Please wait a moment and try again.";
                    }
                    return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data(errorMessage != null ? errorMessage : "An error occurred while streaming")
                            .build());
                });
    }

    private void saveAssistantMessageAndUpdateTitle(
            Long conversationId,
            String content,
            String model,
            String titleSource,
            AtomicReference<Long> messageIdRef) {

        Conversation conv = conversationRepository.findById(conversationId).orElse(null);
        if (conv != null) {
            Message assistantMessage = MessageFactory.createAssistantMessage(conv, content, model);
            Message saved = messageRepository.save(assistantMessage);
            messageIdRef.set(saved.getId());

            ConversationHelper.updateConversationTitleIfNeeded(
                    conv, titleSource, conversationRepository);
        }
    }
}
