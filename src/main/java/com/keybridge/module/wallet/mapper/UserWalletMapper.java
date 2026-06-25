package com.keybridge.module.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keybridge.module.wallet.entity.UserWallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface UserWalletMapper extends BaseMapper<UserWallet> {

    @Update("""
            UPDATE user_wallet
            SET balance_usd = balance_usd + #{amount},
                total_recharged_usd = total_recharged_usd + #{amount}
            WHERE user_id = #{userId}
            """)
    int credit(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
