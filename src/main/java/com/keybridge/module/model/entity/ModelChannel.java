package com.keybridge.module.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("model_channel")
public class ModelChannel {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull
    private Long modelId;
    @NotNull
    private Long providerId;
    @NotNull
    private Long credentialId;
    @NotBlank
    private String upstreamModel;
    private Integer priority;
    private Integer weight;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
