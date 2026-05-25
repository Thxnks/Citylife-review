package com.citylife.controller;

import com.citylife.annotation.RateLimit;
import com.citylife.dto.Result;
import com.citylife.service.IVoucherOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import java.util.concurrent.TimeUnit;

@Tag(name = "秒杀订单", description = "秒杀优惠券下单与订单查询")
@Validated
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Resource
    private IVoucherOrderService voucherOrderService;

    @Operation(summary = "秒杀优惠券", description = "用户秒杀指定优惠券，返回订单ID。每个用户每秒最多1次请求")
    @RateLimit(key = "seckill", rate = 1, rateInterval = 1, rateIntervalUnit = TimeUnit.SECONDS,
            message = "You are clicking too fast, please try later")
    @PostMapping("seckill/{id}")
    public Result<Long> seckillVoucher(
            @Parameter(description = "优惠券ID") @PathVariable("id") @Min(1) Long voucherId) {
        return voucherOrderService.seckillVoucher(voucherId);
    }

    @Operation(summary = "查询订单状态", description = "查询秒杀订单的处理状态")
    @GetMapping("/{id}")
    public Result<?> queryOrderStatus(
            @Parameter(description = "订单ID") @PathVariable("id") @Min(1) Long orderId) {
        return voucherOrderService.queryOrderStatus(orderId);
    }
}
