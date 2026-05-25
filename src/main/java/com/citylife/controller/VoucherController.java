package com.citylife.controller;

import com.citylife.dto.Result;
import com.citylife.entity.Voucher;
import com.citylife.service.IVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

@Tag(name = "优惠券", description = "优惠券与秒杀券管理")
@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Resource
    private IVoucherService voucherService;

    @Operation(summary = "新增秒杀券")
    @PostMapping("seckill")
    public Result<Long> addSeckillVoucher(@RequestBody @Valid Voucher voucher) {
        voucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }

    @Operation(summary = "新增普通券")
    @PostMapping
    public Result<Long> addVoucher(@RequestBody @Valid Voucher voucher) {
        voucherService.save(voucher);
        return Result.ok(voucher.getId());
    }

    @Operation(summary = "查询商铺下的优惠券列表")
    @GetMapping("/list/{shopId}")
    public Result<?> queryVoucherOfShop(
            @Parameter(description = "商铺ID") @PathVariable("shopId") Long shopId) {
        return voucherService.queryVoucherOfShop(shopId);
    }
}
