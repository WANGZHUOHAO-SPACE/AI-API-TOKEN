package com.keybridge.module.log.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keybridge.module.log.entity.RequestLog;
import com.keybridge.module.log.dto.InvocationLogCommand;
import com.keybridge.module.log.mapper.RequestLogMapper;
import com.keybridge.module.log.service.RequestLogService;
import com.keybridge.module.statistics.entity.UsageDaily;
import com.keybridge.module.statistics.mapper.UsageDailyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RequestLogServiceImpl extends ServiceImpl<RequestLogMapper, RequestLog> implements RequestLogService {

    private final UsageDailyMapper usageDailyMapper;

    @Override
    @Transactional
    public void recordInvocation(InvocationLogCommand command) {
        RequestLog requestLog = new RequestLog();
        requestLog.setRequestId(command.requestId());
        requestLog.setUserId(command.userId());
        requestLog.setProviderId(command.providerId());
        requestLog.setProviderCode(command.providerCode());
        requestLog.setCredentialId(command.credentialId());
        requestLog.setModelName(command.modelName());
        requestLog.setInputTokens(command.inputTokens());
        requestLog.setOutputTokens(command.outputTokens());
        requestLog.setTotalTokens(command.totalTokens());
        requestLog.setCost(command.cost());
        requestLog.setDurationMs(command.durationMs());
        requestLog.setStatusCode(command.statusCode());
        requestLog.setSuccess(command.success() ? 1 : 0);
        requestLog.setErrorMessage(command.errorMessage());
        save(requestLog);

        UsageDaily usage = new UsageDaily();
        usage.setUsageDate(LocalDate.now(java.time.ZoneId.of("Asia/Shanghai")));
        usage.setUserId(command.userId());
        usage.setProviderId(command.providerId());
        usage.setProviderCode(command.providerCode());
        usage.setModelName(command.modelName());
        usage.setRequestCount(1L);
        usage.setSuccessCount(command.success() ? 1L : 0L);
        usage.setFailureCount(command.success() ? 0L : 1L);
        usage.setInputTokens((long) command.inputTokens());
        usage.setOutputTokens((long) command.outputTokens());
        usage.setTotalTokens((long) command.totalTokens());
        usage.setTotalDurationMs(command.durationMs());
        usage.setTotalCost(command.cost());
        usageDailyMapper.upsert(usage);
    }
}
