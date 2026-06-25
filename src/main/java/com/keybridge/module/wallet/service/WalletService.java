package com.keybridge.module.wallet.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keybridge.common.exception.BusinessException;
import com.keybridge.module.wallet.config.PaymentProperties;
import com.keybridge.module.wallet.dto.CreateRechargeRequest;
import com.keybridge.module.wallet.dto.ExchangeRateResponse;
import com.keybridge.module.wallet.dto.WalletResponse;
import com.keybridge.module.wallet.entity.RechargeOrder;
import com.keybridge.module.wallet.entity.UserWallet;
import com.keybridge.module.wallet.mapper.RechargeOrderMapper;
import com.keybridge.module.wallet.mapper.UserWalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class WalletService {

    private static final Set<String> METHODS = Set.of("ALIPAY", "WECHAT", "USDT");
    private final UserWalletMapper walletMapper;
    private final RechargeOrderMapper orderMapper;
    private final ExchangeRateService exchangeRateService;
    private final PaymentProperties paymentProperties;

    public WalletResponse getWallet(Long userId) {
        UserWallet wallet = ensureWallet(userId);
        return new WalletResponse(wallet.getBalanceUsd(), wallet.getTotalRechargedUsd());
    }

    @Transactional
    public RechargeOrder createRechargeOrder(Long userId, CreateRechargeRequest request) {
        String method = request.paymentMethod().trim().toUpperCase();
        if (!METHODS.contains(method)) {
            throw new BusinessException("不支持的充值方式");
        }
        ensureWallet(userId);
        BigDecimal amountUsd = request.amountUsd().setScale(2, RoundingMode.HALF_UP);
        ExchangeRateResponse rate = exchangeRateService.getUsdCnyRate();
        RechargeOrder order = new RechargeOrder();
        order.setOrderNo(createOrderNo());
        order.setUserId(userId);
        order.setPaymentMethod(method);
        order.setAmountUsd(amountUsd);
        order.setStatus("PENDING");
        if ("USDT".equals(method)) {
            order.setPayAmount(amountUsd);
            order.setPayCurrency("USDT");
            order.setExchangeRate(BigDecimal.ONE);
            order.setPaymentNetwork(paymentProperties.usdtNetwork());
            order.setPaymentAddress(paymentProperties.usdtAddress());
        } else {
            order.setPayAmount(amountUsd.multiply(rate.rate()).setScale(2, RoundingMode.HALF_UP));
            order.setPayCurrency("CNY");
            order.setExchangeRate(rate.rate());
        }
        orderMapper.insert(order);
        return orderMapper.selectById(order.getId());
    }

    public IPage<RechargeOrder> listUserOrders(Long userId, long page, long size) {
        return orderMapper.selectPage(new Page<>(page, size),
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RechargeOrder>()
                        .eq(RechargeOrder::getUserId, userId)
                        .orderByDesc(RechargeOrder::getCreatedAt));
    }

    public IPage<RechargeOrder> listAllOrders(long page, long size, String status) {
        Page<RechargeOrder> result = new Page<>(page, size);
        return orderMapper.selectPage(result, new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RechargeOrder>()
                .eq(status != null && !status.isBlank(), RechargeOrder::getStatus, status)
                .orderByDesc(RechargeOrder::getCreatedAt));
    }

    @Transactional
    public void confirmOrder(Long orderId, Long reviewerId) {
        RechargeOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "充值订单不存在");
        if (orderMapper.review(orderId, "SUCCESS", reviewerId, LocalDateTime.now()) != 1) {
            throw new BusinessException("该订单已处理，请勿重复操作");
        }
        ensureWallet(order.getUserId());
        if (walletMapper.credit(order.getUserId(), order.getAmountUsd()) != 1) {
            throw new BusinessException("余额入账失败");
        }
    }

    @Transactional
    public void rejectOrder(Long orderId, Long reviewerId) {
        if (orderMapper.selectById(orderId) == null) throw new BusinessException(404, "充值订单不存在");
        if (orderMapper.review(orderId, "REJECTED", reviewerId, LocalDateTime.now()) != 1) {
            throw new BusinessException("该订单已处理，请勿重复操作");
        }
    }

    private UserWallet ensureWallet(Long userId) {
        UserWallet wallet = walletMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserWallet>()
                .eq(UserWallet::getUserId, userId));
        if (wallet != null) return wallet;
        UserWallet created = new UserWallet();
        created.setUserId(userId);
        created.setBalanceUsd(BigDecimal.ZERO);
        created.setTotalRechargedUsd(BigDecimal.ZERO);
        try {
            walletMapper.insert(created);
            return created;
        } catch (Exception ignored) {
            return walletMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserWallet>()
                    .eq(UserWallet::getUserId, userId));
        }
    }

    private String createOrderNo() {
        return "RC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(100000, 999999);
    }
}
