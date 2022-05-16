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
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        Set<Object> viewCount = redisTemplate.opsForZSet().reverseRange("ViewCount", 0, 9);
        Set<Object> tagCount = redisTemplate.opsForZSet().reverseRange("TagCount", 0, 19);

        if(viewCount.size() == 0 || tagCount.size() == 0){
            List<Blog> blogList = blogDao.findBlogList(null);
            for(Blog blog : blogList){
                redisTemplate.opsForZSet().add("ViewCount",blog.getBlogId().toString() + ","+ blog.getBlogTitle(),blog.getBlogViews());
                String[] tags = blog.getBlogTags().split(",");
                for(String tag : tags){
                    BlogTag blogTag = blogTagDao.selectByTagName(tag);
                    redisTemplate.opsForZSet().incrementScore("TagCount",blogTag.getTagId().toString()+","+tag,1);
                }
            }
        }
    }
}
