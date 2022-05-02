package com.hjwsblog.hjwsblog.service;

import com.hjwsblog.hjwsblog.entity.BlogCategory;
import com.hjwsblog.hjwsblog.util.PageQueryUtil;
import com.hjwsblog.hjwsblog.util.PageResult;

import java.util.List;

public interface CategoryService {
    int getTotalCategories();

    PageResult getBlogCategoryPage(PageQueryUtil pageUtil);

    boolean saveCategory(String categoryName,String categoryIcon);

    boolean updateCategory(Integer categoryId,String categoryName,String categoryIcon);

    boolean deleteBatch(Integer[] ids);

    List<BlogCategory> getAllCategories();
}
