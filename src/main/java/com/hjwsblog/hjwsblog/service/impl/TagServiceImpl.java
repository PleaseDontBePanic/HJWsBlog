package com.hjwsblog.hjwsblog.service.impl;

import com.hjwsblog.hjwsblog.Dao.BlogTagDao;
import com.hjwsblog.hjwsblog.Dao.BlogTagRelationDao;
import com.hjwsblog.hjwsblog.entity.Blog;
import com.hjwsblog.hjwsblog.entity.BlogTag;
import com.hjwsblog.hjwsblog.entity.BlogTagCount;
import com.hjwsblog.hjwsblog.service.TagService;
import com.hjwsblog.hjwsblog.util.PageQueryUtil;
import com.hjwsblog.hjwsblog.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private BlogTagDao blogTagDao;

    @Autowired
    private BlogTagRelationDao blogTagRelationDao;

    @Override
    public int getTotalTags() {
        return blogTagDao.getTotalTags();
    }

    @Override
    public PageResult getBlogTagPage(PageQueryUtil pageUtil) {
        List<BlogTag> tagList = blogTagDao.findTagList(pageUtil);
        int totalTags = blogTagDao.getTotalTags();
        PageResult pageResult = new PageResult(tagList, totalTags, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public boolean saveTag(String tagName) {
        BlogTag tag = blogTagDao.selectByTagName(tagName);
        if(tag == null){
            tag = new BlogTag();
            tag.setTagName(tagName);
            return blogTagDao.insertSelective(tag) > 0;
        }
        return false;
    }

    @Override
    public boolean deleteBatch(Integer[] ids) {
        List<Long> rels = blogTagRelationDao.selectDistinctTagIds(ids);
        if(!CollectionUtils.isEmpty(rels)){
            return false;
        }
        return blogTagDao.deleteBatch(ids) > 0;
    }

    /**
     * 返回Tags及其对应的使用次数
     * @return
     */
    @Override
    public List<BlogTagCount> getBlogTagCountForIndex() {
        return blogTagDao.getTagCount();
    }


}
