package com.macro.mall.search.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author hu
 * @create 2022/2/13
 */
@Configuration
@MapperScan(basePackages = {"com.macro.mall.mapper","com.macro.mall.search.dao"})
public class MybatisConfig {
}