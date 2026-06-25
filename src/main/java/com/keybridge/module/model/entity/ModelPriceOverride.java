package com.keybridge.module.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("model_price_override")
public class ModelPriceOverride {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String modelCode;
    private String inputPrice;
    private String cachedPrice;
    private String outputPrice;
    private String extraPrice;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
