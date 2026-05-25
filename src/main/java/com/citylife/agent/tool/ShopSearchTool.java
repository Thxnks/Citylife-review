package com.citylife.agent.tool;

import cn.hutool.json.JSONUtil;
import com.citylife.service.IShopSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopSearchTool {

    private final IShopSearchService shopSearchService;

    @Tool(description = "搜索店铺。支持关键词（店铺名/区域/地址）、店铺类型ID筛选、排序方式（score评分/sold销量/price价格）、分页。返回店铺列表含高亮信息和距离。")
    public String searchShops(
            @ToolParam(description = "搜索关键词，匹配店铺名称、区域、地址") String keyword,
            @ToolParam(description = "店铺类型ID，如1=美食,2=休闲娱乐,3=购物等") Integer typeId,
            @ToolParam(description = "排序方式") String sortBy,
            @ToolParam(description = "页码，从1开始，默认1") Integer page,
            @ToolParam(description = "每页条数，默认10，最大50") Integer size) {

        String kw = keyword != null ? keyword : "";
        String sort = sortBy != null ? sortBy : "score";
        int p = page != null ? page : 1;
        int s = Math.min(size != null ? size : 10, 20);
        Long typeIdLong = typeId != null ? typeId.longValue() : null;

        return JSONUtil.toJsonStr(
                shopSearchService.search(kw, typeIdLong, p, s, sort, null, null)
        );
    }
}
