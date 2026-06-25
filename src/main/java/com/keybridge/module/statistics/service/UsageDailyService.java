package com.keybridge.module.statistics.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keybridge.module.statistics.entity.UsageDaily;
import com.keybridge.module.statistics.dto.StatisticsBreakdown;
import com.keybridge.module.statistics.dto.StatisticsSummary;
import com.keybridge.module.statistics.dto.StatisticsTrendPoint;

import java.time.LocalDate;
import java.util.List;

public interface UsageDailyService extends IService<UsageDaily> {
    StatisticsSummary summary(LocalDate startDate, LocalDate endDate, Long userId);
    List<StatisticsTrendPoint> trend(LocalDate startDate, LocalDate endDate, Long userId);
    List<StatisticsBreakdown> providerBreakdown(LocalDate startDate, LocalDate endDate, Long userId);
    List<StatisticsBreakdown> modelBreakdown(LocalDate startDate, LocalDate endDate, Long userId);
}
