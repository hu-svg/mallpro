package com.macro.mall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author hu
 * @create 2022/2/13
 */
@SpringBootApplication(scanBasePackages = "com.macro.mall")
public class mallSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(mallSearchApplication.class,args);
    }
}