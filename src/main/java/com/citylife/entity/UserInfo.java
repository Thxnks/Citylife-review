package com.citylife.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @since 2021-12-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_user_info")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 鍩庡競鍚嶇О
     */
    private String city;

    /**
     */
    private String introduce;

    /**
     * 绮変笣鏁伴噺
     */
    private Integer fans;

    /**
     */
    private Integer followee;

    /**
     */
    private Boolean gender;

    /**
     */
    private LocalDate birthday;

    /**
     */
    private Integer credits;

    /**
     */
    private Boolean level;

    /**
     * 鍒涘缓鏃堕棿
     */
    private LocalDateTime createTime;

    /**
     * 鏇存柊鏃堕棿
     */
    private LocalDateTime updateTime;


}
