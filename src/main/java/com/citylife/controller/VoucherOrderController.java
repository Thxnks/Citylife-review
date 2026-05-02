package com.citylife.controller;

import com.citylife.dto.Result;
import com.citylife.service.IVoucherOrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Resource
    private IVoucherOrderService voucherOrderService;

    @PostMapping("seckill/{id}")
    public Result<Long> seckillVoucher(@PathVariable("id") @Min(1) Long voucherId) {
        return voucherOrderService.seckillVoucher(voucherId);
    }

    @GetMapping("/{id}")
    public Result<?> queryOrderStatus(@PathVariable("id") @Min(1) Long orderId) {
        return voucherOrderService.queryOrderStatus(orderId);
    }
}
