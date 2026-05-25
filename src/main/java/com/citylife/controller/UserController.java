package com.citylife.controller;

import cn.hutool.core.bean.BeanUtil;
import com.citylife.dto.LoginFormDTO;
import com.citylife.dto.Result;
import com.citylife.dto.UserDTO;
import com.citylife.entity.User;
import com.citylife.entity.UserInfo;
import com.citylife.service.IUserInfoService;
import com.citylife.service.IUserService;
import com.citylife.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

@Slf4j
@Tag(name = "用户", description = "登录、签到、个人信息")
@Validated
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    @Operation(summary = "发送验证码", description = "向指定手机号发送登录验证码")
    @PostMapping("code")
    public Result<?> sendCode(
            @Parameter(description = "手机号") @RequestParam("phone") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "Invalid phone number") String phone,
            HttpSession session) {
        return userService.sendCode(phone, session);
    }

    @Operation(summary = "登录", description = "验证码登录或密码登录")
    @PostMapping("/login")
    public Result<?> login(@RequestBody @Valid LoginFormDTO loginForm, HttpSession session) {
        return userService.login(loginForm, session);
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.fail("Logout is not implemented");
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserDTO> me() {
        return Result.ok(UserHolder.getUser());
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/info/{id}")
    public Result<?> info(@Parameter(description = "用户ID") @PathVariable("id") Long userId) {
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        return Result.ok(info);
    }

    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    public Result<UserDTO> queryUserById(@Parameter(description = "用户ID") @PathVariable("id") Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        return Result.ok(BeanUtil.copyProperties(user, UserDTO.class));
    }

    @Operation(summary = "签到")
    @PostMapping("/sign")
    public Result<?> sign() {
        return userService.sign();
    }

    @Operation(summary = "签到统计")
    @GetMapping("/sign/count")
    public Result<?> signCount() {
        return userService.signCount();
    }
}
