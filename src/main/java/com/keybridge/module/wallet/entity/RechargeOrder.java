package com.keybridge.module.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("recharge_order")
public class RechargeOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private String paymentMethod;
    private BigDecimal amountUsd;
    private BigDecimal payAmount;
    private String payCurrency;
    private BigDecimal exchangeRate;
    private String paymentNetwork;
    private String paymentAddress;
    private String status;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableField(exist = false)
    private String username;
}
