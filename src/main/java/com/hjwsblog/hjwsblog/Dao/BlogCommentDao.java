package com.hjwsblog.hjwsblog.Dao;

import com.hjwsblog.hjwsblog.entity.BlogComment;
import com.hjwsblog.hjwsblog.util.PageQueryUtil;

import java.util.List;
import java.util.Map;

public interface BlogCommentDao {
    int getTotalBlogComments(Map map);

    List<BlogComment> findBlogCommentList(PageQueryUtil pageQueryUtil);

    int checkDone(Integer[] ids);

    BlogComment selectByPrimaryKey(Long commentId);

    int updateByPrimaryKeySelective(BlogComment record);

    int deleteBatch(Integer[] ids);

    int insertSelective(BlogComment record);
}
