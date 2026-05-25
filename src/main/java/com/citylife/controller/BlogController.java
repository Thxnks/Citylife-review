package com.citylife.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.citylife.dto.Result;
import com.citylife.dto.UserDTO;
import com.citylife.entity.Blog;
import com.citylife.service.IBlogService;
import com.citylife.utils.SystemConstants;
import com.citylife.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "探店笔记", description = "笔记发布、点赞、评论、关注流")
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService;

    @Operation(summary = "发布笔记")
    @PostMapping
    public Result<?> saveBlog(@RequestBody @Valid Blog blog) {
        return blogService.saveBlog(blog);
    }

    @Operation(summary = "点赞笔记")
    @PutMapping("/like/{id}")
    public Result<?> likeBlog(@Parameter(description = "笔记ID") @PathVariable("id") Long id) {
        return blogService.likeBlog(id);
    }

    @Operation(summary = "查询我的笔记")
    @GetMapping("/of/me")
    public Result<List<Blog>> queryMyBlog(
            @Parameter(description = "页码") @RequestParam(value = "current", defaultValue = "1") Integer current) {
        UserDTO user = UserHolder.getUser();
        Page<Blog> page = blogService.query()
                .eq("user_id", user.getId())
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        return Result.ok(page.getRecords());
    }

    @Operation(summary = "热门笔记")
    @GetMapping("/hot")
    public Result<?> queryHotBlog(
            @Parameter(description = "页码") @RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryHotBlog(current);
    }

    @Operation(summary = "根据ID查询笔记")
    @GetMapping("/{id}")
    public Result<?> queryBlogById(@Parameter(description = "笔记ID") @PathVariable("id") Long id) {
        return blogService.queryBlogById(id);
    }

    @Operation(summary = "查询笔记点赞用户")
    @GetMapping("/likes/{id}")
    public Result<?> queryBlogLikes(@Parameter(description = "笔记ID") @PathVariable("id") Long id) {
        return blogService.queryBlogLikes(id);
    }

    @Operation(summary = "查询用户笔记列表")
    @GetMapping("/of/user")
    public Result<List<Blog>> queryBlogByUserId(
            @Parameter(description = "页码") @RequestParam(value = "current", defaultValue = "1") Integer current,
            @Parameter(description = "用户ID") @RequestParam("id") Long id) {
        Page<Blog> page = blogService.query()
                .eq("user_id", id)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        return Result.ok(page.getRecords());
    }

    @Operation(summary = "关注流笔记", description = "滚动分页查询关注用户的笔记")
    @GetMapping("/of/follow")
    public Result<?> queryBlogOfFollow(
            @Parameter(description = "上次查询的最后一条ID") @RequestParam("lastId") Long max,
            @Parameter(description = "偏移量") @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return blogService.queryBlogOfFollow(max, offset);
    }
}
