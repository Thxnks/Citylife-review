package com.citylife.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.citylife.dto.Result;
import com.citylife.entity.ShopType;
import com.citylife.mapper.ShopTypeMapper;
import com.citylife.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.citylife.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;
import static com.citylife.utils.RedisConstants.CACHE_SHOP_TYPE_TTL;

@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        String json = stringRedisTemplate.opsForValue().get(CACHE_SHOP_TYPE_KEY);
        if (StrUtil.isNotBlank(json)) {
            List<ShopType> typeList = JSONUtil.toList(json, ShopType.class);
            return Result.ok(typeList);
        }

        List<ShopType> typeList = query().orderByAsc("sort").list();
        stringRedisTemplate.opsForValue().set(
                CACHE_SHOP_TYPE_KEY,
                JSONUtil.toJsonStr(typeList),
                CACHE_SHOP_TYPE_TTL + ThreadLocalRandom.current().nextLong(1, 6),
                TimeUnit.MINUTES
        );
        return Result.ok(typeList);
    }

    @Override
    public Result refreshTypeListCache() {
        stringRedisTemplate.delete(CACHE_SHOP_TYPE_KEY);
        return Result.ok();
    }
}
