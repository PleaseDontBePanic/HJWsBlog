package com.hjwsblog.hjwsblog;

import com.hjwsblog.hjwsblog.Dao.BlogCommentDao;
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
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

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


}
