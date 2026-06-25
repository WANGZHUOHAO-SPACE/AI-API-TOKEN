package com.keybridge.module.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_model")
public class AiModel {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotBlank
    private String modelCode;
    @NotBlank
    private String displayName;
    private String modelType;
    @PositiveOrZero
    private BigDecimal inputPrice;
    @PositiveOrZero
    private BigDecimal outputPrice;
    @Positive
    private Integer maxTokens;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
