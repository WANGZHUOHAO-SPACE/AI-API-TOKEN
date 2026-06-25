package com.keybridge.module.log.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keybridge.module.log.entity.RequestLog;
import com.keybridge.module.log.dto.InvocationLogCommand;

public interface RequestLogService extends IService<RequestLog> {
    void recordInvocation(InvocationLogCommand command);
}
