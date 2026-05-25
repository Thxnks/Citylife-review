package com.citylife.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.citylife.document.ShopDocument;
import com.citylife.dto.Result;
import com.citylife.dto.ShopSearchResultDTO;
import com.citylife.entity.Shop;
import com.citylife.service.IShopSearchService;
import com.citylife.service.IShopService;
import com.citylife.utils.SystemConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShopSearchServiceImpl implements IShopSearchService {

    private static final int MAX_SEARCH_PAGE_SIZE = 50;

    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    @Resource
    private IShopService shopService;

    @Override
    public Result<?> search(String keyword, Long typeId, Integer current, Integer size, String sortBy, Double x, Double y) {
        int pageNo = current == null || current < 1 ? 1 : current;
        int pageSize = size == null || size < 1 ? SystemConstants.DEFAULT_PAGE_SIZE : Math.min(size, MAX_SEARCH_PAGE_SIZE);

        Criteria criteria;
        if (StrUtil.isBlank(keyword)) {
            criteria = new Criteria();
        } else {
            criteria = new Criteria("name").matches(keyword).boost(3.0f)
                    .or(new Criteria("area").matches(keyword).boost(2.0f))
                    .or(new Criteria("address").matches(keyword));
        }
        if (typeId != null) {
            criteria = criteria.and(new Criteria("typeId").is(typeId));
        }

        CriteriaQuery query = new CriteriaQuery(criteria)
                .setPageable(PageRequest.of(pageNo - 1, pageSize));

        if (StrUtil.isNotBlank(keyword)) {
            HighlightField nameField = new HighlightField("name");
            HighlightField areaField = new HighlightField("area");
            HighlightField addressField = new HighlightField("address");
            HighlightParameters params = HighlightParameters.builder()
                    .withPreTags("<em>")
                    .withPostTags("</em>")
                    .build();
            query.setHighlightQuery(new HighlightQuery(
                    new Highlight(params, List.of(nameField, areaField, addressField)),
                    ShopDocument.class));
        }

        applySort(query, sortBy, x, y);

        SearchHits<ShopDocument> searchHits = elasticsearchOperations.search(query, ShopDocument.class);
        List<ShopSearchResultDTO> records = searchHits.getSearchHits().stream()
                .map(this::toResult)
                .collect(Collectors.toList());
        return Result.ok(records, searchHits.getTotalHits());
    }

    @Override
    public Result<?> rebuildIndex() {
        recreateIndex();
        List<Shop> shops = shopService.list();
        if (CollectionUtil.isEmpty(shops)) {
            return Result.ok(0);
        }
        List<ShopDocument> documents = shops.stream()
                .map(ShopDocument::new)
                .collect(Collectors.toList());
        elasticsearchOperations.save(documents);
        return Result.ok(documents.size());
    }

    @Override
    public void save(Shop shop) {
        ensureIndex();
        elasticsearchOperations.save(new ShopDocument(shop));
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            return;
        }
        elasticsearchOperations.delete(id.toString(), ShopDocument.class);
    }

    private void applySort(CriteriaQuery query, String sortBy, Double x, Double y) {
        if (x != null && y != null) {
            var geoPoint = new org.springframework.data.elasticsearch.core.geo.GeoPoint(y, x);
            query.addSort(org.springframework.data.domain.Sort.by(
                    new org.springframework.data.elasticsearch.core.query.GeoDistanceOrder(
                            "location", geoPoint)));
            return;
        }
        if ("sold".equals(sortBy)) {
            query.addSort(org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Order.desc("sold")));
        } else if ("score".equals(sortBy)) {
            query.addSort(org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Order.desc("score")));
        } else if ("price".equals(sortBy)) {
            query.addSort(org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Order.asc("avgPrice")));
        }
    }

    private ShopSearchResultDTO toResult(SearchHit<ShopDocument> searchHit) {
        ShopSearchResultDTO dto = ShopSearchResultDTO.from(searchHit.getContent());
        Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
        setFirstHighlight(highlightFields, "name", dto::setHighlightedName);
        setFirstHighlight(highlightFields, "area", dto::setHighlightedArea);
        setFirstHighlight(highlightFields, "address", dto::setHighlightedAddress);

        List<Object> sortValues = new ArrayList<>(searchHit.getSortValues());
        if (!sortValues.isEmpty() && sortValues.get(0) instanceof Number) {
            dto.setDistance(((Number) sortValues.get(0)).doubleValue());
        }
        return dto;
    }

    private void setFirstHighlight(Map<String, List<String>> highlightFields, String fieldName, java.util.function.Consumer<String> consumer) {
        List<String> highlights = highlightFields.get(fieldName);
        if (CollectionUtil.isNotEmpty(highlights)) {
            consumer.accept(highlights.get(0));
        }
    }

    private void recreateIndex() {
        var indexOps = elasticsearchOperations.indexOps(ShopDocument.class);
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.create();
        var mapping = indexOps.createMapping(ShopDocument.class);
        indexOps.putMapping(mapping);
    }

    private void ensureIndex() {
        var indexOps = elasticsearchOperations.indexOps(ShopDocument.class);
        if (!indexOps.exists()) {
            indexOps.create();
            var mapping = indexOps.createMapping(ShopDocument.class);
            indexOps.putMapping(mapping);
        }
    }
}
