package com.keybridge.module.audit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keybridge.module.audit.entity.AuditLog;
import com.keybridge.module.audit.mapper.AuditLogMapper;
import com.keybridge.module.audit.service.AuditLogService;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {
}
