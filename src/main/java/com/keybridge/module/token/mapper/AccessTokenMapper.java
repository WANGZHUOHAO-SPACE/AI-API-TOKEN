package com.keybridge.module.token.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keybridge.module.token.entity.AccessToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccessTokenMapper extends BaseMapper<AccessToken> {
}
