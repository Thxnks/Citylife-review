package com.citylife.service;

import com.citylife.dto.Result;
import com.citylife.enums.VoucherOrderCreateResult;
import com.citylife.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  鏈嶅姟绫?
 * </p>
 *
 * @since 2021-12-22
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result<Long> seckillVoucher(Long voucherId);

    Result<?> queryOrderStatus(Long orderId);

    boolean saveProcessingOrder(VoucherOrder voucherOrder);

    VoucherOrderCreateResult createVoucherOrder(VoucherOrder voucherOrder);
}
