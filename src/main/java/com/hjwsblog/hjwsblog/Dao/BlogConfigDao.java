package com.hjwsblog.hjwsblog.Dao;

import com.hjwsblog.hjwsblog.entity.BlogConfig;

import java.util.List;

public interface BlogConfigDao {
    List<BlogConfig> selectAll();

    BlogConfig selectByPrimaryKey(String configName);

    int updateByPrimaryKeySelective(BlogConfig blogConfig);
}
