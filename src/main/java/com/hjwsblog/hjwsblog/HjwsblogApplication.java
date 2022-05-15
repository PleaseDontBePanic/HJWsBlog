package com.hjwsblog.hjwsblog;

import com.hjwsblog.hjwsblog.Dao.BlogDao;
import com.hjwsblog.hjwsblog.entity.Blog;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import sun.applet.Main;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@SpringBootApplication
@MapperScan("com.hjwsblog.hjwsblog.Dao")
public class HjwsblogApplication {

    public static void main(String[] args) {
        SpringApplication.run(HjwsblogApplication.class, args);
    }

}
