package com.citylife.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("tb_voucher")
public class Voucher implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     */
    private Long shopId;

    /**
     * 浠ｉ噾鍒告爣棰?
     */
    private String title;

    /**
     */
    private String subTitle;

    /**
     */
    private String rules;

    /**
     * 鏀粯閲戦
     */
    private Long payValue;

    /**
     * 鎶垫墸閲戦
     */
    private Long actualValue;

    /**
     */
    private Integer type;

    /**
     */
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
