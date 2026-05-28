package com.edu.enrollment.security;

import java.lang.annotation.*;

/**
 * 自定义注解，用于在Controller方法参数中获取当前登录用户的ID
 * 从JWT token的claims中提取userId
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUserId {
}
