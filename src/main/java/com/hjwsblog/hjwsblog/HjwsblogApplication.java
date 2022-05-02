package com.hjwsblog.hjwsblog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hjwsblog.hjwsblog.Dao")
public class HjwsblogApplication {

    public static void main(String[] args) {
        SpringApplication.run(HjwsblogApplication.class, args);
    }

}
