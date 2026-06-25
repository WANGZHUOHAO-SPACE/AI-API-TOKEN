package com.keybridge.module.statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keybridge.module.statistics.entity.UsageDaily;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.keybridge.module.statistics.dto.StatisticsBreakdown;
import com.keybridge.module.statistics.dto.StatisticsSummary;
import com.keybridge.module.statistics.dto.StatisticsTrendPoint;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UsageDailyMapper extends BaseMapper<UsageDaily> {

    @Insert("""
            INSERT INTO usage_daily (
                usage_date, user_id, model_id, provider_id, provider_code, model_name,
                request_count, success_count, failure_count, input_tokens, output_tokens,
                total_tokens, total_duration_ms, total_cost
            ) VALUES (
                #{usageDate}, #{userId}, #{modelId}, #{providerId}, #{providerCode}, #{modelName},
                #{requestCount}, #{successCount}, #{failureCount}, #{inputTokens}, #{outputTokens},
                #{totalTokens}, #{totalDurationMs}, #{totalCost}
            )
            ON DUPLICATE KEY UPDATE
                provider_id = COALESCE(VALUES(provider_id), provider_id),
                request_count = request_count + VALUES(request_count),
                success_count = success_count + VALUES(success_count),
                failure_count = failure_count + VALUES(failure_count),
                input_tokens = input_tokens + VALUES(input_tokens),
                output_tokens = output_tokens + VALUES(output_tokens),
                total_tokens = total_tokens + VALUES(total_tokens),
                total_duration_ms = total_duration_ms + VALUES(total_duration_ms),
                total_cost = total_cost + VALUES(total_cost)
            """)
    int upsert(UsageDaily usageDaily);

    @Select("""
            SELECT
                COALESCE(SUM(request_count), 0) AS request_count,
                COALESCE(SUM(success_count), 0) AS success_count,
                COALESCE(SUM(failure_count), 0) AS failure_count,
                COALESCE(SUM(total_tokens), 0) AS total_tokens,
                COALESCE(SUM(total_duration_ms), 0) AS total_duration_ms,
                CASE WHEN SUM(request_count) > 0
                    THEN ROUND(SUM(total_duration_ms) / SUM(request_count), 2) ELSE 0 END AS average_duration_ms,
                CASE WHEN SUM(request_count) > 0
                    THEN ROUND(SUM(success_count) * 100.0 / SUM(request_count), 2) ELSE 0 END AS success_rate
            FROM usage_daily
            WHERE usage_date BETWEEN #{startDate} AND #{endDate}
              AND (#{userId} IS NULL OR user_id = #{userId})
            """)
    StatisticsSummary summary(@Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate,
                              @Param("userId") Long userId);

    @Select("""
            SELECT
                usage_date AS date,
                SUM(request_count) AS request_count,
                SUM(success_count) AS success_count,
                SUM(failure_count) AS failure_count,
                SUM(total_tokens) AS total_tokens,
                CASE WHEN SUM(request_count) > 0
                    THEN ROUND(SUM(total_duration_ms) / SUM(request_count)) ELSE 0 END AS average_duration_ms
            FROM usage_daily
            WHERE usage_date BETWEEN #{startDate} AND #{endDate}
              AND (#{userId} IS NULL OR user_id = #{userId})
            GROUP BY usage_date
            ORDER BY usage_date
            """)
    List<StatisticsTrendPoint> trend(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     @Param("userId") Long userId);

    @Select("""
            SELECT
                provider_code AS name,
                SUM(request_count) AS request_count,
                SUM(success_count) AS success_count,
                SUM(failure_count) AS failure_count,
                SUM(total_tokens) AS total_tokens,
                CASE WHEN SUM(request_count) > 0
                    THEN ROUND(SUM(total_duration_ms) / SUM(request_count)) ELSE 0 END AS average_duration_ms
            FROM usage_daily
            WHERE usage_date BETWEEN #{startDate} AND #{endDate}
              AND (#{userId} IS NULL OR user_id = #{userId})
            GROUP BY provider_code
            ORDER BY request_count DESC
            LIMIT 10
            """)
    List<StatisticsBreakdown> providerBreakdown(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                @Param("userId") Long userId);

    @Select("""
            SELECT
                model_name AS name,
                SUM(request_count) AS request_count,
                SUM(success_count) AS success_count,
                SUM(failure_count) AS failure_count,
                SUM(total_tokens) AS total_tokens,
                CASE WHEN SUM(request_count) > 0
                    THEN ROUND(SUM(total_duration_ms) / SUM(request_count)) ELSE 0 END AS average_duration_ms
            FROM usage_daily
            WHERE usage_date BETWEEN #{startDate} AND #{endDate}
              AND (#{userId} IS NULL OR user_id = #{userId})
            GROUP BY model_name
            ORDER BY request_count DESC
            LIMIT 10
            """)
    List<StatisticsBreakdown> modelBreakdown(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate,
                                             @Param("userId") Long userId);
}
