package com.hjwsblog.hjwsblog;

import com.hjwsblog.hjwsblog.Dao.BlogTagDao;
import com.hjwsblog.hjwsblog.entity.BlogTag;
import com.hjwsblog.hjwsblog.service.AdminUserService;
import com.hjwsblog.hjwsblog.service.BlogService;
import com.hjwsblog.hjwsblog.service.CategoryService;
import com.hjwsblog.hjwsblog.service.TagService;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HjwsblogApplicationTests {
    @Autowired
    private DataSource dataSource;

    @Autowired
    BlogTagDao blogTagDao;
    @Resource
    CategoryService categoryService;
    @Resource
    TagService tagService;
    @Resource
    BlogService blogService;
    @Resource
    AdminUserService adminUserService;

    @Autowired
    private SqlSession sqlSession;

    @Autowired
    private RedisTemplate redisTemplate;



    @Test
    public void TestAdmin(){
        BlogTag tag = blogTagDao.selectByTagName("测试");
        System.out.println();
    }

    @Test
    public void TestBlog(){
        System.out.println(tagService.getTotalTags());
    }

    @Test
    public void MyBatisTest(){
        System.out.println(sqlSession != null);
        System.out.println(sqlSession.getClass());
    }

    @Test
    public void DBConnect() throws SQLException {
        Connection connection = dataSource.getConnection();
        System.out.println(connection!=null);
        System.out.println(dataSource.getClass());
        connection.close();
    }

    @Test
    public void TestGit(){
        System.out.println("Giiit");
        System.out.println("GGGGiit");
    }

    @Test
    public void TestRedis(){
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        Set<ZSetOperations.TypedTuple<Object>> count = redisTemplate.opsForZSet().reverseRangeWithScores("TagCount", 0, -1);
        Iterator<ZSetOperations.TypedTuple<Object>> iterator = count.iterator();
        while (iterator.hasNext())
        {
            ZSetOperations.TypedTuple<Object> typedTuple = iterator.next();
            System.out.println("value:" + typedTuple.getValue() + "score:" + typedTuple.getScore());
        }

//
//        Set<Object> tagCount = redisTemplate.opsForZSet().reverseRange("TagCount", 0, 19);
//        for(Object tag : tagCount){
//            System.out.println(tag.toString());
//        }
//        redisTemplate.opsForZSet().incrementScore("ZZZ","hjw",100);
//        Set<String> zzz = redisTemplate.opsForZSet().reverseRange("ZZZ",0,0);
//        Set<Object> viewCount = redisTemplate.opsForZSet().reverseRange("ViewCount", 0, 9);
//        System.out.println(zzz);
//        for(Object s : zzz){
//            System.out.println(s.toString());
//        }
//        redisTemplate.opsForValue().set("name","hjw");
//        String name = (String)redisTemplate.opsForValue().get("name");
//        System.out.println(name);
    }

    @Test
    public void TestList(){
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.opsForList().leftPush("test","hjw");
        redisTemplate.opsForList().leftPush("test","zqy");
        List test = redisTemplate.opsForList().range("test", 0, 1);
        for(Object name : test){
            System.out.println(name.toString());
        }
    }

}
