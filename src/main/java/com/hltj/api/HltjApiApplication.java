package com.hltj.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hltj.api.mapper")
public class  HltjApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HltjApiApplication.class, args);
    }
}