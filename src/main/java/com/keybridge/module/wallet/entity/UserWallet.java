package com.keybridge.module.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_wallet")
public class UserWallet {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal balanceUsd;
    private BigDecimal totalRechargedUsd;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
