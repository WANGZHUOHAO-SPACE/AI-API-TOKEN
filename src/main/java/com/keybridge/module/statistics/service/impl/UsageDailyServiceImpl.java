package com.keybridge.module.statistics.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keybridge.module.statistics.entity.UsageDaily;
import com.keybridge.module.statistics.dto.StatisticsBreakdown;
import com.keybridge.module.statistics.dto.StatisticsSummary;
import com.keybridge.module.statistics.dto.StatisticsTrendPoint;
import com.keybridge.module.statistics.mapper.UsageDailyMapper;
import com.keybridge.module.statistics.service.UsageDailyService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UsageDailyServiceImpl extends ServiceImpl<UsageDailyMapper, UsageDaily> implements UsageDailyService {

    @Override
    public StatisticsSummary summary(LocalDate startDate, LocalDate endDate, Long userId) {
        return baseMapper.summary(startDate, endDate, userId);
    }

    @Override
    public List<StatisticsTrendPoint> trend(LocalDate startDate, LocalDate endDate, Long userId) {
        return baseMapper.trend(startDate, endDate, userId);
    }

    @Override
    public List<StatisticsBreakdown> providerBreakdown(LocalDate startDate, LocalDate endDate, Long userId) {
        return baseMapper.providerBreakdown(startDate, endDate, userId);
    }

    @Override
    public List<StatisticsBreakdown> modelBreakdown(LocalDate startDate, LocalDate endDate, Long userId) {
        return baseMapper.modelBreakdown(startDate, endDate, userId);
    }
}
