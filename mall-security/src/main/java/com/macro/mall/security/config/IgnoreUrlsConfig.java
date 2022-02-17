package com.macro.mall.security.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hu
 * @create 2022/2/13
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "security.ignore")
public class IgnoreUrlsConfig {
    private List<String> urls = new ArrayList<>();
}