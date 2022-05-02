package com.hjwsblog.hjwsblog.service.impl;

import com.hjwsblog.hjwsblog.Dao.BlogCategoryDao;
import com.hjwsblog.hjwsblog.Dao.BlogDao;
import com.hjwsblog.hjwsblog.entity.BlogCategory;
import com.hjwsblog.hjwsblog.service.CategoryService;
import com.hjwsblog.hjwsblog.util.PageQueryUtil;
import com.hjwsblog.hjwsblog.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private BlogCategoryDao blogCategoryDao;

    @Autowired
    private BlogDao blogDao;

    @Override
    public int getTotalCategories() {
        return blogCategoryDao.getTotalCategories();
    }

    @Override
    public PageResult getBlogCategoryPage(PageQueryUtil pageUtil) {
        List<BlogCategory> categoryList = blogCategoryDao.findCategoryList(pageUtil);
        int total = blogCategoryDao.getTotalCategories();
        PageResult pageResult = new PageResult(categoryList, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public boolean saveCategory(String categoryName, String categoryIcon) {
        //先根据新增的分类名查找有无已存在的同名分类，若无则继续添加
        BlogCategory selected = blogCategoryDao.selectByCategoryName(categoryName);
        if(selected == null){
            BlogCategory blogCategory = new BlogCategory();
            blogCategory.setCategoryName(categoryName);
            blogCategory.setCategoryIcon(categoryIcon);
            return blogCategoryDao.insertSelective(blogCategory) > 0;
        }
        return false;
    }

    @Override
    public boolean updateCategory(Integer categoryId, String categoryName, String categoryIcon) {
        BlogCategory blogCategory = blogCategoryDao.selectByPrimaryKey(categoryId);
        if(blogCategory != null){
            blogCategory.setCategoryName(categoryName);
            blogCategory.setCategoryIcon(categoryIcon);
            blogDao.updateBlogCategory(categoryName, categoryId, categoryId);
            return blogCategoryDao.updateByPrimaryKeySelective(blogCategory) > 0;
        }
        return false;
    }

    @Override
    public boolean deleteBatch(Integer[] ids) {
        if (ids.length < 1) {
            return false;
        }
        for(Integer id : ids){
            blogDao.updateBlogCategory("默认分类",0,id);
        }
        return blogCategoryDao.deleteBatch(ids) > 0;
    }

    @Override
    public List<BlogCategory> getAllCategories() {
        return blogCategoryDao.getAllCategories();
    }
}
