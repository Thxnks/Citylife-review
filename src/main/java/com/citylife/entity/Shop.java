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
@TableName("tb_shop")
public class Shop implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "Shop name is required")
    private String name;

    @NotNull(message = "Type ID is required")
    private Long typeId;

    private String images;

    private String area;

    private String address;

    /**
     * 缁忓害
     */
    private Double x;

    /**
     * 缁村害
     */
    private Double y;

    /**
     */
    private Long avgPrice;

    /**
     */
    private Integer sold;

    /**
     */
    private Integer comments;

    /**
     */
    private Integer score;

    /**
     */
    private String openHours;

    /**
     * 鍒涘缓鏃堕棿
     */
    private LocalDateTime createTime;

    /**
     * 鏇存柊鏃堕棿
     */
    private LocalDateTime updateTime;


    @TableField(exist = false)
    private Double distance;
}
