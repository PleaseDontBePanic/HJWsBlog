package com.hjwsblog.hjwsblog.service;

import com.hjwsblog.hjwsblog.entity.BlogComment;
import com.hjwsblog.hjwsblog.util.PageQueryUtil;
import com.hjwsblog.hjwsblog.util.PageResult;

public interface CommentService {
    int getTotalComments();

    PageResult getCommentsPage(PageQueryUtil pageUtil);

    boolean checkDone(Integer[] ids);

    Boolean reply(Long commentId, String replyBody);

    Boolean deleteBatch(Integer[] ids);

    Boolean addComment(BlogComment blogComment);

    PageResult getCommentPageByBlogIdAndPageNum(Long blogId, Integer commentPage);
}
