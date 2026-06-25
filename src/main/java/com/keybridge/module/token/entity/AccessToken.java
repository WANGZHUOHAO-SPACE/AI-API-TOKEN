package com.keybridge.module.token.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("access_token")
public class AccessToken {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String tokenPrefix;
    @JsonIgnore
    private String tokenHash;
    private Integer status;
    private LocalDateTime expiresAt;
    private Long requestLimit;
    private Long tokenLimit;
    private Long usedTokens;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
