package com.keybridge.module.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keybridge.module.log.entity.RequestLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RequestLogMapper extends BaseMapper<RequestLog> {
}
