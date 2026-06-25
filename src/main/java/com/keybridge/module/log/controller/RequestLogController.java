package com.keybridge.module.log.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keybridge.common.api.ApiResponse;
import com.keybridge.common.exception.BusinessException;
import com.keybridge.common.security.CurrentUserService;
import com.keybridge.module.log.entity.RequestLog;
import com.keybridge.module.log.service.RequestLogService;
import com.keybridge.module.user.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/request-logs")
@RequiredArgsConstructor
public class RequestLogController {

    private final RequestLogService logService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<Page<RequestLog>> list(
            Authentication authentication,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer statusCode,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BusinessException("分页参数不合法，size 最大为 100");
        }
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException("startTime 不能晚于 endTime");
        }
        SysUser user = currentUserService.requireUser(authentication);
        return ApiResponse.success(logService.lambdaQuery()
                .eq(!"ADMIN".equals(user.getRole()), RequestLog::getUserId, user.getId())
                .eq(provider != null && !provider.isBlank(), RequestLog::getProviderCode, provider)
                .eq(model != null && !model.isBlank(), RequestLog::getModelName, model)
                .eq(statusCode != null, RequestLog::getStatusCode, statusCode)
                .eq(success != null, RequestLog::getSuccess, Boolean.TRUE.equals(success) ? 1 : 0)
                .ge(startTime != null, RequestLog::getCreatedAt, startTime)
                .le(endTime != null, RequestLog::getCreatedAt, endTime)
                .orderByDesc(RequestLog::getCreatedAt)
                .page(new Page<>(page, size)));
    }

    @GetMapping("/{requestId}")
    public ApiResponse<RequestLog> detail(Authentication authentication, @PathVariable String requestId) {
        SysUser user = currentUserService.requireUser(authentication);
        RequestLog log = logService.lambdaQuery()
                .eq(RequestLog::getRequestId, requestId)
                .eq(!"ADMIN".equals(user.getRole()), RequestLog::getUserId, user.getId())
                .one();
        if (log == null) {
            throw new BusinessException(404, "请求日志不存在");
        }
        return ApiResponse.success(log);
    }
}
