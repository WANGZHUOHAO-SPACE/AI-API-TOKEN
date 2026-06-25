package com.keybridge.module.provider.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_provider")
public class AiProvider {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String code;
    @NotBlank
    private String baseUrl;
    private String protocolType;
    private Integer status;
    @Positive
    private Integer timeoutSeconds;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
