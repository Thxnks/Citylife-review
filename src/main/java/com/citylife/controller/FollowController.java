package com.citylife.controller;

import com.citylife.dto.Result;
import com.citylife.service.IFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@Tag(name = "关注", description = "关注/取关、共同关注")
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;

    @Operation(summary = "关注/取关用户")
    @PutMapping("/{id}/{isFollow}")
    public Result<?> follow(
            @Parameter(description = "目标用户ID") @PathVariable("id") Long followUserId,
            @Parameter(description = "true关注/false取关") @PathVariable("isFollow") Boolean isFollow) {
        return followService.follow(followUserId, isFollow);
    }

    @Operation(summary = "是否已关注")
    @GetMapping("/or/not/{id}")
    public Result<?> isFollow(@Parameter(description = "目标用户ID") @PathVariable("id") Long followUserId) {
        return followService.isFollow(followUserId);
    }

    @Operation(summary = "共同关注")
    @GetMapping("/common/{id}")
    public Result<?> followCommons(@Parameter(description = "目标用户ID") @PathVariable("id") Long id) {
        return followService.followCommons(id);
    }
}
