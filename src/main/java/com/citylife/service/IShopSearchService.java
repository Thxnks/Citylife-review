package com.citylife.service;

import com.citylife.dto.Result;
import com.citylife.entity.Shop;

public interface IShopSearchService {

    Result<?> search(String keyword, Long typeId, Integer current, Integer size, String sortBy, Double x, Double y);

    Result<?> rebuildIndex();

    void save(Shop shop);

    void deleteById(Long id);
}
