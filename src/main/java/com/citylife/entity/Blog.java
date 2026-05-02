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
@TableName("tb_blog")
public class Blog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 鍟嗘埛id
     */
    private Long shopId;
    /**
     */
    private Long userId;
    /**
     */
    @TableField(exist = false)
    private String icon;
    /**
     */
    @TableField(exist = false)
    private String name;
    /**
     * 鏄惁鐐硅禐杩囦簡
     */
    @TableField(exist = false)
    private Boolean isLike;

    /**
     * 鏍囬
     */
    private String title;

    /**
     */
    private String images;

    /**
     */
    private String content;

    /**
     * 鐐硅禐鏁伴噺
     */
    private Integer liked;

    /**
     */
    private Integer comments;

    /**
     * 鍒涘缓鏃堕棿
     */
    private LocalDateTime createTime;

    /**
     * 鏇存柊鏃堕棿
     */
    private LocalDateTime updateTime;


}
