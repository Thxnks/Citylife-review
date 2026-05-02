package com.citylife.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.citylife.dto.LoginFormDTO;
import com.citylife.dto.Result;
import com.citylife.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  鏈嶅姟绫?
 * </p>
 *
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();

}
