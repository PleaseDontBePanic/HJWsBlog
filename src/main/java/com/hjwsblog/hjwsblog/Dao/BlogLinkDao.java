package com.hjwsblog.hjwsblog.Dao;

import com.hjwsblog.hjwsblog.entity.BlogLink;
import com.hjwsblog.hjwsblog.util.PageQueryUtil;

import java.util.List;

public interface BlogLinkDao {
    int getTotalLinks();

    List<BlogLink> findLinkList(PageQueryUtil pageUtil);

    int insertSelective(BlogLink link);

    BlogLink selectById(Integer id);

    int deleteBatch(Integer[] ids);

    int updateByPrimaryKeySelective(BlogLink link);
}
