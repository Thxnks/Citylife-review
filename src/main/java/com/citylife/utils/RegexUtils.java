package com.citylife.utils;

import cn.hutool.core.util.StrUtil;

/**
 */
public class RegexUtils {
    /**
     * 鏄惁鏄棤鏁堟墜鏈烘牸寮?
     */
    public static boolean isPhoneInvalid(String phone){
        return mismatch(phone, RegexPatterns.PHONE_REGEX);
    }
    /**
     * 鏄惁鏄棤鏁堥偖绠辨牸寮?
     */
    public static boolean isEmailInvalid(String email){
        return mismatch(email, RegexPatterns.EMAIL_REGEX);
    }

    /**
     */
    public static boolean isCodeInvalid(String code){
        return mismatch(code, RegexPatterns.VERIFY_CODE_REGEX);
    }

    private static boolean mismatch(String str, String regex){
        if (StrUtil.isBlank(str)) {
            return true;
        }
        return !str.matches(regex);
    }
}
