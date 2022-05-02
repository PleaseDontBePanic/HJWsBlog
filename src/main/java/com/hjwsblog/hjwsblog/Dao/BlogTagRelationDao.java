package com.hjwsblog.hjwsblog.Dao;

import com.hjwsblog.hjwsblog.entity.BlogTagRelation;

import java.util.List;

public interface BlogTagRelationDao {
    List<Long> selectDistinctTagIds(Integer[] ids);

    int bathInsert(BlogTagRelation blogTagRelation);

    int deleteByBlogId(Long BlogId);
}
