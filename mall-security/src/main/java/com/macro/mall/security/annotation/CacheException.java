package com.macro.mall.security.annotation;

import java.lang.annotation.*;

/**
 * @author hu
 * @create 2022/2/12
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheException {
}
