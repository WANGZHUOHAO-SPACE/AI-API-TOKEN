package com.keybridge.module.log.service;

import com.keybridge.module.log.dto.InvocationLogCommand;
import com.keybridge.module.log.entity.RequestLog;
import com.keybridge.module.log.mapper.RequestLogMapper;
import com.keybridge.module.log.service.impl.RequestLogServiceImpl;
import com.keybridge.module.statistics.entity.UsageDaily;
import com.keybridge.module.statistics.mapper.UsageDailyMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RequestLogServiceImplTest {

    @Test
    void shouldRecordLogAndAggregateDailyUsage() {
        RequestLogMapper logMapper = mock(RequestLogMapper.class);
        UsageDailyMapper usageMapper = mock(UsageDailyMapper.class);
        RequestLogServiceImpl service = new RequestLogServiceImpl(usageMapper);
        ReflectionTestUtils.setField(service, "baseMapper", logMapper);
        when(logMapper.insert(any(RequestLog.class))).thenReturn(1);
        when(usageMapper.upsert(any(UsageDaily.class))).thenReturn(1);

        service.recordInvocation(new InvocationLogCommand(
                "req-1", 10L, 2L, "openai", 5L, "gpt-demo",
                10, 20, 30, BigDecimal.ZERO, 120, 200, true, null));

        ArgumentCaptor<RequestLog> logCaptor = ArgumentCaptor.forClass(RequestLog.class);
        verify(logMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getUserId()).isEqualTo(10L);
        assertThat(logCaptor.getValue().getProviderCode()).isEqualTo("openai");
        assertThat(logCaptor.getValue().getModelName()).isEqualTo("gpt-demo");
        assertThat(logCaptor.getValue().getDurationMs()).isEqualTo(120L);

        ArgumentCaptor<UsageDaily> usageCaptor = ArgumentCaptor.forClass(UsageDaily.class);
        verify(usageMapper).upsert(usageCaptor.capture());
        assertThat(usageCaptor.getValue().getRequestCount()).isEqualTo(1L);
        assertThat(usageCaptor.getValue().getSuccessCount()).isEqualTo(1L);
        assertThat(usageCaptor.getValue().getTotalTokens()).isEqualTo(30L);
    }
}
