package com.hjwsblog.hjwsblog.redisInit;

import com.hjwsblog.hjwsblog.Dao.BlogDao;
import com.hjwsblog.hjwsblog.Dao.BlogTagDao;
import com.hjwsblog.hjwsblog.entity.Blog;
import com.hjwsblog.hjwsblog.entity.BlogTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;


/**
 * 项目初始化操作（在SpringBoot项目运行时执行）
 * 为了在第一次启用Redis时写入Zset
 */
@Component
public class RedisInit implements ApplicationRunner {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private BlogDao blogDao;

    @Autowired
    private BlogTagDao blogTagDao;

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        序列化
        redisTemplate.setValueSerializer(new StringRedisSerializer());
//        检查项目是否是第一次使用Redis
        Set<Object> viewCount = redisTemplate.opsForZSet().reverseRange("ViewCount", 0, 9);
        Set<Object> tagCount = redisTemplate.opsForZSet().reverseRange("TagCount", 0, 19);
//        如果是则执行初始化操作，不是不执行任何操作
        if(viewCount.size() == 0 || tagCount.size() == 0){
//            获取所有Blog
            List<Blog> blogList = blogDao.findBlogList(null);
            for(Blog blog : blogList){
//                将ViewCount的Zset传入SQL查到的存储的访问量，Key以ID+Title的格式存储
                redisTemplate.opsForZSet().add("ViewCount",blog.getBlogId().toString() + ","+ blog.getBlogTitle(),blog.getBlogViews());
                String[] tags = blog.getBlogTags().split(",");
                for(String tag : tags){
//                    获取存储此Tag的类
                    BlogTag blogTag = blogTagDao.selectByTagName(tag);
//                    此Tag的使用次数++
                    redisTemplate.opsForZSet().incrementScore("TagCount",blogTag.getTagId().toString()+","+tag,1);
                }
            }
        }
//        是否使用过Redis来存储最新博客，如果未使用过则初始化对应Redis
        if(redisTemplate.opsForList().size("NewBlog") == 0){
            List<Blog> blogList = blogDao.findBlogList(null);
            for(Blog blog : blogList){
                redisTemplate.opsForList().rightPush("NewBlog",blog.getBlogId().toString() + ","+ blog.getBlogTitle());
            }
        }

    }
}
