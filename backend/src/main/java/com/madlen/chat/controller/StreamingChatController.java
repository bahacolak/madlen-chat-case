package com.madlen.chat.controller;

import com.madlen.chat.dto.ChatRequest;
import com.madlen.chat.service.StreamingChatService;
import com.madlen.chat.service.UserService;
import com.madlen.chat.util.AuthenticationHelper;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class StreamingChatController {

    private final StreamingChatService streamingChatService;
    private final UserService userService;

    public StreamingChatController(StreamingChatService streamingChatService,
                                   UserService userService) {
        this.streamingChatService = streamingChatService;
        this.userService = userService;
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@Valid @RequestBody ChatRequest request,
                                                     Authentication authentication) {
        Long userId = AuthenticationHelper.getUserIdFromAuthentication(authentication, userService);
        return streamingChatService.streamChat(request, userId);
    }
}
