package com.hjwsblog.hjwsblog.Dao;

import com.hjwsblog.hjwsblog.entity.Blog;
import com.hjwsblog.hjwsblog.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BlogDao {
    int getTotalBlogs(PageQueryUtil pageUtil);

    int updateBlogCategory(@Param("categoryName") String categoryName, @Param("categoryId") Integer categoryId,@Param("id")Integer id);

    int insertSelective(Blog blog);

    List<Blog> findBlogList(PageQueryUtil pageUtil);

    Blog getBlogById(Long BlogId);

    int updateByPrimaryKeySelective(Blog blog);

    int deleteBatchById(Integer[] ids);

    Long getIdByBlogTitle(String BlogTitle);

    List<Blog> findBlogListByType(int type , int limit);

    int getTotalBlogsByTagId(PageQueryUtil pageUtil);

    Blog selectBySubUrl(String subUrl);

    List<Blog> getBlogsPageByTagId(PageQueryUtil pageUtil);

    int getViewCount();
}
