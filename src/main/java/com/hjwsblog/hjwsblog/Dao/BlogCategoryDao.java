package com.hjwsblog.hjwsblog.Dao;

import com.hjwsblog.hjwsblog.entity.BlogCategory;
import com.hjwsblog.hjwsblog.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BlogCategoryDao {
    int getTotalCategories();

    List<BlogCategory> findCategoryList(PageQueryUtil pageUtil);

    BlogCategory selectByCategoryName(String name);

    int insertSelective(BlogCategory blogCategory);

    BlogCategory selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(BlogCategory blogCategory);

    int deleteBatch(Integer[] ids);

    List<BlogCategory> getAllCategories();

    List<BlogCategory> selectByCategoryIds(@Param("categoryIds") List<Integer> categoryIds);
}
