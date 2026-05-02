package com.citylife.service;

import com.citylife.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;
import com.citylife.dto.Result;

/**
 * <p>
 *  鏈嶅姟绫?
 * </p>
 *
 * @since 2021-12-22
 */
public interface IShopTypeService extends IService<ShopType> {

    Result queryTypeList();

    Result refreshTypeListCache();
}
