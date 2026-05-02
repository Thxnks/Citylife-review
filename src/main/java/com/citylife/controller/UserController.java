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
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;

@Slf4j
@Validated
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    @PostMapping("code")
    public Result<?> sendCode(
            @RequestParam("phone") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "Invalid phone number") String phone,
            HttpSession session) {
        return userService.sendCode(phone, session);
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody @Valid LoginFormDTO loginForm, HttpSession session) {
        return userService.login(loginForm, session);
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.fail("Logout is not implemented");
    }

    @GetMapping("/me")
    public Result<UserDTO> me() {
        return Result.ok(UserHolder.getUser());
    }

    @GetMapping("/info/{id}")
    public Result<?> info(@PathVariable("id") Long userId) {
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        return Result.ok(info);
    }

    @GetMapping("/{id}")
    public Result<UserDTO> queryUserById(@PathVariable("id") Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        return Result.ok(BeanUtil.copyProperties(user, UserDTO.class));
    }

    @PostMapping("/sign")
    public Result<?> sign() {
        return userService.sign();
    }

    @GetMapping("/sign/count")
    public Result<?> signCount() {
        return userService.signCount();
    }
}
