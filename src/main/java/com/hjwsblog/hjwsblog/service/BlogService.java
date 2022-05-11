package com.hjwsblog.hjwsblog.service;

import com.hjwsblog.hjwsblog.controller.vo.BlogDetailVO;
import com.hjwsblog.hjwsblog.controller.vo.SimpleBlogListVO;
import com.hjwsblog.hjwsblog.entity.Blog;
import com.hjwsblog.hjwsblog.entity.BlogTagCount;
import com.hjwsblog.hjwsblog.util.PageQueryUtil;
import com.hjwsblog.hjwsblog.util.PageResult;

import java.util.List;

public interface BlogService {
    int getTotalBlogs();

    String saveBlog(Blog blog);

    PageResult getBlogsPage(PageQueryUtil pageUtil);

    Blog getBlogById(Long BlogId);

    String updateBlog(Blog blog);

    boolean deleteBatch(Integer[] ids);

    PageResult getBlogsForIndexPage(int page);

    List<SimpleBlogListVO> getBlogListForIndexPage(int type);

    PageResult getBlogsPageByTag(String tagName, Integer page);

    PageResult getBlogsPageByCategory(String categoryName, Integer page);

    PageResult getBlogsPageBySearch(String keyword, Integer page);

    BlogDetailVO getBlogDetailBySubUrl(String subUrl);

    BlogDetailVO getBlogDetail(Long blogId);

    int getViewCount();

    void addJayView();
}
