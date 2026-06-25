package com.keybridge.module.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keybridge.module.wallet.entity.RechargeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface RechargeOrderMapper extends BaseMapper<RechargeOrder> {

    @Update("""
            UPDATE recharge_order
            SET status = #{status}, reviewed_by = #{reviewerId}, reviewed_at = #{reviewedAt}
            WHERE id = #{id} AND status = 'PENDING'
            """)
    int review(@Param("id") Long id,
               @Param("status") String status,
               @Param("reviewerId") Long reviewerId,
               @Param("reviewedAt") LocalDateTime reviewedAt);
}
