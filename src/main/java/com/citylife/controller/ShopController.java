package com.citylife.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.citylife.dto.Result;
import com.citylife.entity.Shop;
import com.citylife.service.IShopSearchService;
import com.citylife.service.IShopService;
import com.citylife.utils.SystemConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "商铺", description = "商铺查询、新增、修改")
@RestController
@Slf4j
@RequestMapping("/shop")
public class ShopController {

    @Resource
    private IShopService shopService;

    @Resource
    private IShopSearchService shopSearchService;

    @Operation(summary = "根据ID查询商铺")
    @GetMapping("/{id}")
    public Result<?> queryShopById(@Parameter(description = "商铺ID") @PathVariable("id") Long id) {
        return shopService.queryById(id);
    }

    @Operation(summary = "新增商铺")
    @PostMapping
    public Result<Long> saveShop(@RequestBody @Valid Shop shop) {
        shopService.save(shop);
        try {
            shopSearchService.save(shop);
        } catch (RuntimeException e) {
            log.warn("failed to sync shop to elasticsearch, shopId: {}", shop.getId(), e);
        }
        return Result.ok(shop.getId());
    }

    @Operation(summary = "更新商铺")
    @PutMapping
    public Result<?> updateShop(@RequestBody @Valid Shop shop) {
        return shopService.update(shop);
    }

    @Operation(summary = "按类型查询商铺", description = "支持按地理位置排序")
    @GetMapping("/of/type")
    public Result<?> queryShopByType(
            @Parameter(description = "商铺类型ID") @RequestParam("typeId") Integer typeId,
            @Parameter(description = "页码") @RequestParam(value = "current", defaultValue = "1") Integer current,
            @Parameter(description = "经度") @RequestParam(value = "x", required = false) Double x,
            @Parameter(description = "纬度") @RequestParam(value = "y", required = false) Double y) {
        return shopService.queryShopByType(typeId, current, x, y);
    }

    @Operation(summary = "按名称搜索商铺")
    @GetMapping("/of/name")
    public Result<List<Shop>> queryShopByName(
            @Parameter(description = "商铺名称") @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "页码") @RequestParam(value = "current", defaultValue = "1") Integer current) {
        Page<Shop> page = shopService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        return Result.ok(page.getRecords());
    }

    @Operation(summary = "ElasticSearch shop search", description = "Keyword search with filter, sort, distance and highlight")
    @GetMapping("/search")
    public Result<?> searchShops(
            @Parameter(description = "Keyword") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "Shop type ID") @RequestParam(value = "typeId", required = false) Long typeId,
            @Parameter(description = "Page number") @RequestParam(value = "current", defaultValue = "1") Integer current,
            @Parameter(description = "Page size") @RequestParam(value = "size", required = false) Integer size,
            @Parameter(description = "Sort by: score/sold/price") @RequestParam(value = "sortBy", required = false) String sortBy,
            @Parameter(description = "Longitude") @RequestParam(value = "x", required = false) Double x,
            @Parameter(description = "Latitude") @RequestParam(value = "y", required = false) Double y) {
        return shopSearchService.search(keyword, typeId, current, size, sortBy, x, y);
    }

    @Operation(summary = "Rebuild ElasticSearch shop index")
    @PostMapping("/search/index/rebuild")
    public Result<?> rebuildShopSearchIndex() {
        return shopSearchService.rebuildIndex();
    }
}
