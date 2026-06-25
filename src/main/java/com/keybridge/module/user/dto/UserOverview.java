package com.keybridge.module.user.dto;

public record UserOverview(long totalUsers,
                           long activeUsers,
                           long adminUsers,
                           long regularUsers) {
}
