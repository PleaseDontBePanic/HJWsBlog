package com.hjwsblog.hjwsblog.service;

import com.hjwsblog.hjwsblog.entity.BlogTag;
import com.hjwsblog.hjwsblog.entity.BlogTagCount;
import com.hjwsblog.hjwsblog.util.PageQueryUtil;
import com.hjwsblog.hjwsblog.util.PageResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TagService {
    int getTotalTags();

    PageResult getBlogTagPage(PageQueryUtil pageUtil);

    boolean saveTag(String tagName);

    boolean deleteBatch(Integer[] ids);

    List<BlogTagCount> getBlogTagCountForIndex();

    List<BlogTagCount> getBlogTagCount();
}
