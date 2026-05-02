package com.citylife.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @since 2021-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_voucher_order")
public class VoucherOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     */
    private Long userId;

    /**
     */
    private Long voucherId;

    /**
     */
    private Integer payType;

    /**
     */
    private Integer status;

    /**
     */
    private LocalDateTime createTime;

    /**
     * 鏀粯鏃堕棿
     */
    private LocalDateTime payTime;

    /**
     * 鏍搁攢鏃堕棿
     */
    private LocalDateTime useTime;

    /**
     */
    private LocalDateTime refundTime;

    /**
     * 鏇存柊鏃堕棿
     */
    private LocalDateTime updateTime;


}
