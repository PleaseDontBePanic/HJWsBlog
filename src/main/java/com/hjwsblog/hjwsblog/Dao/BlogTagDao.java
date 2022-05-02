package com.hjwsblog.hjwsblog.Dao;

import com.hjwsblog.hjwsblog.entity.Blog;
import com.hjwsblog.hjwsblog.entity.BlogTag;
import com.hjwsblog.hjwsblog.entity.BlogTagCount;
import com.hjwsblog.hjwsblog.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BlogTagDao {
    int getTotalTags();

    List<BlogTag> findTagList(PageQueryUtil pageQueryUtil);

    int insertSelective(BlogTag blogTag);

    BlogTag selectByTagName(String tagName);

    int deleteBatch(Integer[] ids);

    List<BlogTagCount> getTagCount();


}
