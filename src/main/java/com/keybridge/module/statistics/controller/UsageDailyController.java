package com.keybridge.module.statistics.controller;

import com.keybridge.common.api.ApiResponse;
import com.keybridge.common.exception.BusinessException;
import com.keybridge.common.security.CurrentUserService;
import com.keybridge.module.statistics.dto.StatisticsBreakdown;
import com.keybridge.module.statistics.dto.StatisticsSummary;
import com.keybridge.module.statistics.dto.StatisticsTrendPoint;
import com.keybridge.module.statistics.service.UsageDailyService;
import com.keybridge.module.user.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class UsageDailyController {

    private final UsageDailyService usageService;
    private final CurrentUserService currentUserService;

    @GetMapping("/summary")
    public ApiResponse<StatisticsSummary> summary(Authentication authentication,
                                                  @RequestParam(defaultValue = "7") int days,
                                                  @RequestParam(required = false) Long userId) {
        QueryScope scope = scope(authentication, days, userId);
        return ApiResponse.success(usageService.summary(scope.startDate(), scope.endDate(), scope.userId()));
    }

    @GetMapping("/trend")
    public ApiResponse<List<StatisticsTrendPoint>> trend(Authentication authentication,
                                                          @RequestParam(defaultValue = "7") int days,
                                                          @RequestParam(required = false) Long userId) {
        QueryScope scope = scope(authentication, days, userId);
        return ApiResponse.success(usageService.trend(scope.startDate(), scope.endDate(), scope.userId()));
    }

    @GetMapping("/providers")
    public ApiResponse<List<StatisticsBreakdown>> providers(Authentication authentication,
                                                             @RequestParam(defaultValue = "7") int days,
                                                             @RequestParam(required = false) Long userId) {
        QueryScope scope = scope(authentication, days, userId);
        return ApiResponse.success(usageService.providerBreakdown(scope.startDate(), scope.endDate(), scope.userId()));
    }

    @GetMapping("/models")
    public ApiResponse<List<StatisticsBreakdown>> models(Authentication authentication,
                                                          @RequestParam(defaultValue = "7") int days,
                                                          @RequestParam(required = false) Long userId) {
        QueryScope scope = scope(authentication, days, userId);
        return ApiResponse.success(usageService.modelBreakdown(scope.startDate(), scope.endDate(), scope.userId()));
    }

    private QueryScope scope(Authentication authentication, int days, Long requestedUserId) {
        if (days < 1 || days > 90) {
            throw new BusinessException("days 必须在 1 到 90 之间");
        }
        SysUser currentUser = currentUserService.requireUser(authentication);
        Long userId = "ADMIN".equals(currentUser.getRole()) ? requestedUserId : currentUser.getId();
        LocalDate endDate = LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"));
        return new QueryScope(endDate.minusDays(days - 1L), endDate, userId);
    }

    private record QueryScope(LocalDate startDate, LocalDate endDate, Long userId) {
    }
}
