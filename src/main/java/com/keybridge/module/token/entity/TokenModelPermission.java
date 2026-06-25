package com.keybridge.module.token.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("token_model_permission")
public class TokenModelPermission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long accessTokenId;
    private Long modelId;
    private LocalDateTime createdAt;
}
