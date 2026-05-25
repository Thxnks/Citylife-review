package com.citylife.agent.tool;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.citylife.entity.Blog;
import com.citylife.service.IBlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BlogTool {

    private final IBlogService blogService;

    @Tool(description = "获取某个店铺的用户点评/日记列表，包含点评内容、点赞数、用户昵称。用于了解店铺口碑。")
    public String getShopBlogs(
            @ToolParam(description = "店铺ID") Long shopId,
            @ToolParam(description = "返回条数，默认5，最大10") Integer limit) {
        if (shopId == null) {
            return JSONUtil.toJsonStr(Map.of("error", "shopId不能为空"));
        }
        int pageSize = Math.min(limit != null ? limit : 5, 10);

        Page<Blog> page = new Page<>(1, pageSize);
        LambdaQueryWrapper<Blog> wrapper = new LambdaQueryWrapper<Blog>()
                .eq(Blog::getShopId, shopId)
                .orderByDesc(Blog::getLiked);
        List<Blog> blogs = blogService.page(page, wrapper).getRecords();

        return JSONUtil.toJsonStr(blogs);
    }
}
