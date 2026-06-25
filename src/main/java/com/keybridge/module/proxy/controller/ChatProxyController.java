package com.keybridge.module.proxy.controller;

import com.keybridge.common.api.ApiResponse;
import com.keybridge.common.security.CurrentUserService;
import com.keybridge.module.proxy.dto.ChatProxyRequest;
import com.keybridge.module.proxy.dto.ChatProxyResponse;
import com.keybridge.module.proxy.service.ChatProxyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proxy")
@RequiredArgsConstructor
public class ChatProxyController {

    private final ChatProxyService chatProxyService;
    private final CurrentUserService currentUserService;

    @PostMapping("/chat")
    public ApiResponse<ChatProxyResponse> chat(Authentication authentication,
                                               @Valid @RequestBody ChatProxyRequest request) {
        return ApiResponse.success(chatProxyService.chat(
                request, currentUserService.requireUser(authentication)));
    }
}
