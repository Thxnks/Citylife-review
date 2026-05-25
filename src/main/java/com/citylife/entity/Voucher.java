package com.citylife.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@TableName("tb_voucher")
public class Voucher implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotNull(message = "Shop ID is required")
    private Long shopId;

    @NotBlank(message = "Voucher title is required")
    private String title;

    private String subTitle;

    private String rules;

    @NotNull(message = "Pay value is required")
    @Min(value = 1, message = "Pay value must be at least 1 cent")
    private Long payValue;

    @NotNull(message = "Actual value is required")
    @Min(value = 1, message = "Actual value must be at least 1 cent")
    private Long actualValue;

    @NotNull(message = "Voucher type is required")
    private Integer type;

    private Integer status;
    /**
     */
    @TableField(exist = false)
    private Integer stock;

    /**
     */
    @TableField(exist = false)
    private LocalDateTime beginTime;

    /**
     * 澶辨晥鏃堕棿
     */
    @TableField(exist = false)
    private LocalDateTime endTime;

    /**
     * 鍒涘缓鏃堕棿
     */
    private LocalDateTime createTime;


    /**
     * 鏇存柊鏃堕棿
     */
    private LocalDateTime updateTime;


}
