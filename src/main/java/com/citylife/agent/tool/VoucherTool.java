package com.citylife.agent.tool;

import cn.hutool.json.JSONUtil;
import com.citylife.service.IVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class VoucherTool {

    private final IVoucherService voucherService;

    @Tool(description = "获取指定店铺的优惠券列表，含秒杀券和普通券。返回券的标题、原价、优惠价、类型、库存等信息。")
    public String getShopVouchers(
            @ToolParam(description = "店铺ID") Long shopId) {
        if (shopId == null) {
            return JSONUtil.toJsonStr(Map.of("error", "shopId不能为空"));
        }
        return JSONUtil.toJsonStr(voucherService.queryVoucherOfShop(shopId));
    }
}
