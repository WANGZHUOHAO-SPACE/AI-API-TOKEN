package com.keybridge.module.wallet.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keybridge.common.api.ApiResponse;
import com.keybridge.common.security.CurrentUserService;
import com.keybridge.module.user.entity.SysUser;
import com.keybridge.module.wallet.config.PaymentProperties;
import com.keybridge.module.wallet.dto.CreateRechargeRequest;
import com.keybridge.module.wallet.dto.ExchangeRateResponse;
import com.keybridge.module.wallet.dto.WalletResponse;
import com.keybridge.module.wallet.entity.RechargeOrder;
import com.keybridge.module.wallet.service.ExchangeRateService;
import com.keybridge.module.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final ExchangeRateService exchangeRateService;
    private final CurrentUserService currentUserService;
    private final PaymentProperties paymentProperties;

    @GetMapping
    public ApiResponse<WalletResponse> wallet(Authentication authentication) {
        return ApiResponse.success(walletService.getWallet(currentUser(authentication).getId()));
    }

    @GetMapping("/exchange-rate")
    public ApiResponse<ExchangeRateResponse> exchangeRate() {
        return ApiResponse.success(exchangeRateService.getUsdCnyRate());
    }

    @GetMapping("/payment-config")
    public ApiResponse<Map<String, String>> paymentConfig() {
        return ApiResponse.success(Map.of(
                "usdtAddress", paymentProperties.usdtAddress(),
                "usdtNetwork", paymentProperties.usdtNetwork()
        ));
    }

    @PostMapping("/recharge-orders")
    public ApiResponse<RechargeOrder> createOrder(Authentication authentication,
                                                   @Valid @RequestBody CreateRechargeRequest request) {
        return ApiResponse.success(walletService.createRechargeOrder(currentUser(authentication).getId(), request));
    }

    @GetMapping("/recharge-orders")
    public ApiResponse<IPage<RechargeOrder>> userOrders(Authentication authentication,
                                                        @RequestParam(defaultValue = "1") long page,
                                                        @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.success(walletService.listUserOrders(currentUser(authentication).getId(), page, size));
    }

    @GetMapping("/admin/recharge-orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<IPage<RechargeOrder>> allOrders(@RequestParam(defaultValue = "1") long page,
                                                       @RequestParam(defaultValue = "20") long size,
                                                       @RequestParam(required = false) String status) {
        return ApiResponse.success(walletService.listAllOrders(page, size, status));
    }

    @PatchMapping("/admin/recharge-orders/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> confirm(Authentication authentication, @PathVariable Long id) {
        walletService.confirmOrder(id, currentUser(authentication).getId());
        return ApiResponse.success();
    }

    @PatchMapping("/admin/recharge-orders/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> reject(Authentication authentication, @PathVariable Long id) {
        walletService.rejectOrder(id, currentUser(authentication).getId());
        return ApiResponse.success();
    }

    private SysUser currentUser(Authentication authentication) {
        return currentUserService.requireUser(authentication);
    }
}
