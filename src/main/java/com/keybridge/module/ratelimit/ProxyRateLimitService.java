package com.keybridge.module.ratelimit;

import com.keybridge.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProxyRateLimitService {

    private static final java.time.ZoneId APP_ZONE = java.time.ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter MINUTE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DefaultRedisScript<Long> LIMIT_SCRIPT = new DefaultRedisScript<>("""
            local userCount = tonumber(redis.call('GET', KEYS[1]) or '0')
            if userCount >= tonumber(ARGV[1]) then
                return -1
            end
            local keyCount = tonumber(redis.call('GET', KEYS[2]) or '0')
            if keyCount >= tonumber(ARGV[2]) then
                return -2
            end
            userCount = redis.call('INCR', KEYS[1])
            if userCount == 1 then
                redis.call('EXPIRE', KEYS[1], tonumber(ARGV[3]))
            end
            keyCount = redis.call('INCR', KEYS[2])
            if keyCount == 1 then
                redis.call('EXPIRE', KEYS[2], tonumber(ARGV[4]))
            end
            return 1
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;

    public void checkAndConsume(Long userId, Long credentialId) {
        ZonedDateTime now = ZonedDateTime.now(APP_ZONE);
        String userKey = "keybridge:limit:user:" + userId + ":" + DAY_FORMAT.format(now);
        String credentialKey = "keybridge:limit:key:" + credentialId + ":" + MINUTE_FORMAT.format(now);
        long userTtl = Math.max(1, Duration.between(now, now.toLocalDate()
                .plusDays(1).atStartOfDay(APP_ZONE)).toSeconds() + 60);

        try {
            Long result = redisTemplate.execute(
                    LIMIT_SCRIPT,
                    List.of(userKey, credentialKey),
                    String.valueOf(properties.userDailyLimit()),
                    String.valueOf(properties.keyMinuteLimit()),
                    String.valueOf(userTtl),
                    "120"
            );
            if (result == null) {
                throw new BusinessException(503, "限流服务暂不可用");
            }
            if (result == -1L) {
                throw new BusinessException(429, "今日调用次数已达到 " + properties.userDailyLimit() + " 次上限");
            }
            if (result == -2L) {
                throw new BusinessException(429, "该 API Key 每分钟最多调用 " + properties.keyMinuteLimit() + " 次");
            }
            if (result != 1L) {
                throw new BusinessException(503, "限流服务返回异常结果");
            }
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BusinessException(503, "限流服务暂不可用");
        }
    }
}
