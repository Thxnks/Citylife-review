package com.citylife.controller;

import com.citylife.dto.Result;
import com.citylife.service.IShopTypeService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    @Resource
    private IShopTypeService typeService;

    @GetMapping("list")
    public Result<?> queryTypeList() {
        return typeService.queryTypeList();
    }

    @DeleteMapping("cache")
    public Result<?> refreshTypeListCache() {
        return typeService.refreshTypeListCache();
    }
}
