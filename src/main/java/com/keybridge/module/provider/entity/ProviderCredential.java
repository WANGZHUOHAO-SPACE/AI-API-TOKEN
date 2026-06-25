package com.keybridge.module.provider.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("provider_credential")
public class ProviderCredential {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long providerId;
    private String name;
    @JsonIgnore
    private String encryptedKey;
    @JsonIgnore
    private String keySuffix;
    private Integer priority;
    private Integer weight;
    private Integer status;
    private Integer failureCount;
    private LocalDateTime lastCheckedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
