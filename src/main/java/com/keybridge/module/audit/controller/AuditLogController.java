package com.keybridge.module.audit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keybridge.common.api.ApiResponse;
import com.keybridge.module.audit.entity.AuditLog;
import com.keybridge.module.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ApiResponse<Page<AuditLog>> list(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.success(auditLogService.lambdaQuery()
                .orderByDesc(AuditLog::getCreatedAt)
                .page(new Page<>(page, size)));
    }
}
