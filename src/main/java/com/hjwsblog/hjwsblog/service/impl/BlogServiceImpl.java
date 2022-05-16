package com.hjwsblog.hjwsblog.service.impl;

import com.hjwsblog.hjwsblog.Dao.*;
import com.hjwsblog.hjwsblog.controller.vo.BlogDetailVO;
import com.hjwsblog.hjwsblog.controller.vo.BlogListVO;
import com.hjwsblog.hjwsblog.controller.vo.SimpleBlogListVO;
import com.hjwsblog.hjwsblog.entity.Blog;
import com.hjwsblog.hjwsblog.entity.BlogCategory;
import com.hjwsblog.hjwsblog.entity.BlogTag;
import com.hjwsblog.hjwsblog.entity.BlogTagRelation;
import com.hjwsblog.hjwsblog.service.BlogService;
import com.hjwsblog.hjwsblog.util.MarkDownUtil;
import com.hjwsblog.hjwsblog.util.PageQueryUtil;
import com.hjwsblog.hjwsblog.util.PageResult;
import com.hjwsblog.hjwsblog.util.PatternUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogCommentDao blogCommentDao;

    @Autowired
    private BlogTagRelationDao blogTagRelationDao;

    @Autowired
    private BlogTagDao blogTagDao;

    @Autowired
    private BlogCategoryDao blogCategoryDao;

    @Autowired
    private BlogDao blogDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public int getTotalBlogs() {
        return blogDao.getTotalBlogs(null);
    }

    @Override
    @Transactional
    public String saveBlog(Blog blog) {
        redisTemplate.setValueSerializer(new StringRedisSerializer());
//        若分类不存在则设为默认分类
        BlogCategory blogCategory = blogCategoryDao.selectByPrimaryKey(blog.getBlogCategoryId());
        if(blogCategory == null){
            blog.setBlogCategoryId(0);
            blog.setBlogCategoryName("默认分类");
        }else{
            blog.setBlogCategoryName(blogCategory.getCategoryName());
            blogCategory.setCategoryRank(blogCategory.getCategoryRank()+1);//将此category的rank值++（rank值即为分类中文章的个数）
        }
//        将Tags以,分开获取
        String[] tags = blog.getBlogTags().split(",");

        if (tags.length > 6) {
            return "标签数量限制为6";
        }
        if(blogDao.insertSelective(blog) > 0){ //Blog存储成功
            Long id = blogDao.getIdByBlogTitle(blog.getBlogTitle());//获取BlogId供后续设置Blog-Tag关系数据库所用
            blog.setBlogId(id);
            List<BlogTag> tagsForInsert = new ArrayList<>();//需新增的Tags
            List<BlogTag> allTags = new ArrayList<>();//存储所有Tags
            for(String nowtag : tags){
                BlogTag tag = blogTagDao.selectByTagName(nowtag);//根据Tag名称查询
                if(tag == null){//为null说明之前不存在，要新建
                    BlogTag blogTag = new BlogTag();
                    blogTag.setTagName(nowtag);
                    tagsForInsert.add(blogTag);
                }else{//已存在则插入allTags
                    allTags.add(tag);
                }
            }
//            将要新建的Tag新建至数据库中
            for(int index = 0 ; index < tagsForInsert.size() ; index++){
                if(blogTagDao.insertSelective(tagsForInsert.get(index)) > 0){
                    //将已经创建成功的Tag对象赋予其id值，之前未插入Tags数据库，无从得知其id
                    tagsForInsert.set(index,blogTagDao.selectByTagName(tagsForInsert.get(index).getTagName()));
                }
            }

            for(String tag : tags){
                BlogTag blogTag = blogTagDao.selectByTagName(tag);
                redisTemplate.opsForZSet().incrementScore("TagCount",blogTag.getTagId().toString()+","+tag,1);
            }

//            非默认分类的分类Rank值++
            if(blogCategory != null){
                blogCategoryDao.updateByPrimaryKeySelective(blogCategory);
            }
            allTags.addAll(tagsForInsert);//将新添加的Tags也加入AllTags中
            List<BlogTagRelation> relas = new ArrayList<>();//新建关系表List
            for(BlogTag tag : allTags){//将每个Blog对应的Tag都建立一张二者的关系表
                BlogTagRelation blogTagRelation = new BlogTagRelation();
                blogTagRelation.setBlogId(blog.getBlogId());
                blogTagRelation.setTagId(tag.getTagId());
                relas.add(blogTagRelation);
            }
            redisTemplate.opsForList().leftPush("NewBlog",blog.getBlogId().toString() + ","+ blog.getBlogTitle());
            for(BlogTagRelation relation : relas){
                //将所有表都存入数据库中
                if(blogTagRelationDao.bathInsert(relation) <= 0){
                    return "保存失败";
                }
            }
        }
        return "success";
    }

    @Override
    public PageResult getBlogsPage(PageQueryUtil pageUtil) {
        List<Blog> blogList = blogDao.findBlogList(pageUtil);
        int totalBlogs = blogDao.getTotalBlogs(null);
        PageResult pageResult = new PageResult(blogList, totalBlogs, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public Blog getBlogById(Long BlogId) {
        return blogDao.getBlogById(BlogId);
    }

    /**
     * 方法抽取
     *
     * @param blog
     * @return
     */
    private BlogDetailVO getBlogDetailVO(Blog blog) {
        if (blog != null && blog.getBlogStatus() == 1) {
            //增加浏览量
            redisTemplate.opsForZSet().incrementScore("ViewCount",blog.getBlogId().toString() + "," + blog.getBlogTitle(),1);
            blog.setBlogViews(blog.getBlogViews() + 1);
            blogDao.updateByPrimaryKeySelective(blog);//更新Blog信息
            BlogDetailVO blogDetailVO = new BlogDetailVO();
//            将查询出的Blog中的内容复制入BlogDetailVO中
            BeanUtils.copyProperties(blog, blogDetailVO);
            //将MarkDown语法转为html格式
            blogDetailVO.setBlogContent(MarkDownUtil.mdToHtml(blogDetailVO.getBlogContent()));
            BlogCategory blogCategory = blogCategoryDao.selectByPrimaryKey(blog.getBlogCategoryId());
            if (blogCategory == null) {//若没有指定category则为其指定默认分类
                blogCategory = new BlogCategory();
                blogCategory.setCategoryId(0);
                blogCategory.setCategoryName("默认分类");
                blogCategory.setCategoryIcon("/admin/dist/img/category/00.png");
            }
            //分类信息
            blogDetailVO.setBlogCategoryIcon(blogCategory.getCategoryIcon());
            if (!StringUtils.isEmpty(blog.getBlogTags())) {
                //标签设置
                List<String> tags = Arrays.asList(blog.getBlogTags().split(","));
                blogDetailVO.setBlogTags(tags);
            }
            //设置评论数
            Map params = new HashMap();
            params.put("blogId", blog.getBlogId());
            params.put("commentStatus", 1);//过滤审核通过的数据
            blogDetailVO.setCommentCount(blogCommentDao.getTotalBlogComments(params));
            return blogDetailVO;
        }
        return null;
    }

    @Override
    @Transactional //其本质是对方法前后进行拦截，然后在目标方法开始之前创建或者加入一个事务，在执行完目标方法之后根据执行情况提交或者回滚事务。
    public String updateBlog(Blog blog) {
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        Blog blogForUpdate = blogDao.getBlogById(blog.getBlogId());
        if (blogForUpdate == null) {
            return "数据不存在";
        }
        String[] oldTags = blogForUpdate.getBlogTags().split(",");
        for(String tag : oldTags){
            BlogTag blogTag = blogTagDao.selectByTagName(tag);
            redisTemplate.opsForZSet().incrementScore("TagCount",blogTag.getTagId().toString()+","+tag,-1);
        }
//        为Blog对象设置相应的新属性
        blogForUpdate.setBlogTitle(blog.getBlogTitle());
        blogForUpdate.setBlogSubUrl(blog.getBlogSubUrl());
        blogForUpdate.setBlogContent(blog.getBlogContent());
        blogForUpdate.setBlogCoverImage(blog.getBlogCoverImage());
        blogForUpdate.setBlogStatus(blog.getBlogStatus());
        blogForUpdate.setEnableComment(blog.getEnableComment());
        BlogCategory blogCategory = blogCategoryDao.selectByPrimaryKey(blog.getBlogCategoryId());
        //        若分类不存在则设为默认分类
        if(blogCategory == null){
            blogForUpdate.setBlogCategoryId(0);
            blogForUpdate.setBlogCategoryName("默认分类");
        }else{
            blogForUpdate.setBlogCategoryName(blogCategory.getCategoryName());
//            若果是新Category则新分类Rank++,而旧分类Rank--
            if(blogForUpdate.getBlogCategoryId() != blog.getBlogCategoryId()){
                blogCategory.setCategoryRank(blogCategory.getCategoryRank()+1);
                BlogCategory PassedCategory = blogCategoryDao.selectByPrimaryKey(blogForUpdate.getBlogCategoryId());
                PassedCategory.setCategoryRank(PassedCategory.getCategoryRank()-1);
                blogCategoryDao.updateByPrimaryKeySelective(PassedCategory);
            }
            blogForUpdate.setBlogCategoryId(blogCategory.getCategoryId());
        }
        //        将Tags以,分开获取
        String[] tags = blog.getBlogTags().split(",");
        if (tags.length > 6) {
            return "标签数量限制为6";
        }

        blogForUpdate.setBlogTags(blog.getBlogTags());
        //新增的tag对象
        List<BlogTag> tagListForInsert = new ArrayList<>();
        //所有的tag对象，用于建立关系数据
        List<BlogTag> allTagsList = new ArrayList<>();
        for (int i = 0; i < tags.length; i++) {
            BlogTag tag = blogTagDao.selectByTagName(tags[i]);
            if (tag == null) {
                //不存在就新增
                BlogTag tempTag = new BlogTag();
                tempTag.setTagName(tags[i]);
                tagListForInsert.add(tempTag);
            } else {
                allTagsList.add(tag);
            }
        }
        //            将要新建的Tag新建至数据库中
        for(int index = 0 ; index < tagListForInsert.size() ; index++){
            if(blogTagDao.insertSelective(tagListForInsert.get(index)) > 0){
//                为新增的Tag赋id值
                tagListForInsert.set(index,blogTagDao.selectByTagName(tagListForInsert.get(index).getTagName()));
            }
        }

        for(String tag : tags){
            BlogTag blogTag = blogTagDao.selectByTagName(tag);
            redisTemplate.opsForZSet().incrementScore("TagCount",blogTag.getTagId().toString()+","+tag,1);
        }

//        所有Tags都汇总
        allTagsList.addAll(tagListForInsert);
        List<BlogTagRelation> relas = new ArrayList<>();
//        建立关系表
        for(BlogTag tag : allTagsList){
            BlogTagRelation blogTagRelation = new BlogTagRelation();
            blogTagRelation.setBlogId(blog.getBlogId());
            blogTagRelation.setTagId(tag.getTagId());
            relas.add(blogTagRelation);
        }
        if (blogCategory != null) {
            blogCategoryDao.updateByPrimaryKeySelective(blogCategory);
        }
//        删除原Blog与Tags的关系
        blogTagRelationDao.deleteByBlogId(blog.getBlogId());
        for(BlogTagRelation rel : relas){
//            建立新关系
            blogTagRelationDao.bathInsert(rel);
        }
//        更新Blog
        if(blogDao.updateByPrimaryKeySelective(blogForUpdate) > 0){
            return "success";
        }
        return "修改失败";

    }

    @Override
    public boolean deleteBatch(Integer[] ids) {
        return blogDao.deleteBatchById(ids) > 0;
    }

    /**
     * 获取指定页码Blog，并将其封装至PageResult中
     * @param page
     * @return
     */
    @Override
    public PageResult getBlogsForIndexPage(int page) {
        Map params = new HashMap();
        params.put("page", page);//当前页面
        params.put("limit", 8);//每页的文章数
        params.put("blogStatus", 1);//过滤发布状态下的数据
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        List<Blog> blogList = blogDao.findBlogList(pageUtil);
        //将包含全部Bog信息的List改为仅包含指定信息的List
        List<BlogListVO> blogListVOS = getBlogListVOsByBlogs(blogList);
        int total = blogDao.getTotalBlogs(pageUtil);
        PageResult pageResult = new PageResult(blogListVOS, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    /**
     * 根据传入的type值返回按发布时间或浏览量排序的BlogList
     * 其中返回的List仅包含id、Title
     * @param type
     * @return
     */
    @Override
    public List<SimpleBlogListVO> getBlogListForIndexPage(int type) {
        List<SimpleBlogListVO> simpleBlogListVOS = new ArrayList<>();
        //找到按要求排序的前九个结果
        List<Blog> blogs = blogDao.findBlogListByType(type, 9);
        if (!CollectionUtils.isEmpty(blogs)) {
            for (Blog blog : blogs) {
                SimpleBlogListVO simpleBlogListVO = new SimpleBlogListVO();
                //复制Blog中id、title信息
                BeanUtils.copyProperties(blog, simpleBlogListVO);
                simpleBlogListVOS.add(simpleBlogListVO);
            }
        }
        return simpleBlogListVOS;
    }

    @Override
    public PageResult getBlogsPageByTag(String tagName, Integer page) {
        if (PatternUtil.validKeyword(tagName)) {
            BlogTag tag = blogTagDao.selectByTagName(tagName);
            if (tag != null && page > 0) {
                Map param = new HashMap();
                param.put("page", page);
                param.put("limit", 9);
                param.put("tagId", tag.getTagId());
                PageQueryUtil pageUtil = new PageQueryUtil(param);
                List<Blog> blogList = blogDao.getBlogsPageByTagId(pageUtil);
                List<BlogListVO> blogListVOS = getBlogListVOsByBlogs(blogList);
                int total = blogDao.getTotalBlogsByTagId(pageUtil);
                PageResult pageResult = new PageResult(blogListVOS, total, pageUtil.getLimit(), pageUtil.getPage());
                return pageResult;
            }
        }
        return null;
    }

    @Override
    public PageResult getBlogsPageByCategory(String categoryName, Integer page) {
        if (PatternUtil.validKeyword(categoryName)) {
            BlogCategory blogCategory = blogCategoryDao.selectByCategoryName(categoryName);
            if ("默认分类".equals(categoryName) && blogCategory == null) {
                blogCategory = new BlogCategory();
                blogCategory.setCategoryId(0);
            }
            if (blogCategory != null && page > 0) {
                Map param = new HashMap();
                param.put("page", page);
                param.put("limit", 9);
                param.put("blogCategoryId", blogCategory.getCategoryId());
                param.put("blogStatus", 1);//过滤发布状态下的数据
                PageQueryUtil pageUtil = new PageQueryUtil(param);
                List<Blog> blogList = blogDao.findBlogList(pageUtil);
                List<BlogListVO> blogListVOS = getBlogListVOsByBlogs(blogList);
                int total = blogDao.getTotalBlogs(pageUtil);
                PageResult pageResult = new PageResult(blogListVOS, total, pageUtil.getLimit(), pageUtil.getPage());
                return pageResult;
            }
        }
        return null;
    }

    @Override
    public PageResult getBlogsPageBySearch(String keyword, Integer page) {
        if (page > 0 && PatternUtil.validKeyword(keyword)) {
            Map param = new HashMap();
            param.put("page", page);
            param.put("limit", 9);
            param.put("keyword", keyword);
            param.put("blogStatus", 1);//过滤发布状态下的数据
            PageQueryUtil pageUtil = new PageQueryUtil(param);
            List<Blog> blogList = blogDao.findBlogList(pageUtil);
            List<BlogListVO> blogListVOS = getBlogListVOsByBlogs(blogList);
            int total = blogDao.getTotalBlogs(pageUtil);
            PageResult pageResult = new PageResult(blogListVOS, total, pageUtil.getLimit(), pageUtil.getPage());
            return pageResult;
        }
        return null;
    }

    @Override
    public BlogDetailVO getBlogDetailBySubUrl(String subUrl) {
        Blog blog = blogDao.selectBySubUrl(subUrl);
        //不为空且状态为已发布
        BlogDetailVO blogDetailVO = getBlogDetailVO(blog);
        if (blogDetailVO != null) {
            return blogDetailVO;
        }
        return null;
    }

    @Override
    public BlogDetailVO getBlogDetail(Long blogId) {
        Blog blog = blogDao.getBlogById(blogId);
        //不为空且状态为已发布
        BlogDetailVO blogDetailVO = getBlogDetailVO(blog);
        if (blogDetailVO != null) {
            return blogDetailVO;
        }
        return null;
    }

    @Override
    public int getViewCount() {
        return blogDao.getViewCount();
    }

    @Override
    public void addJayView() {
        Blog jay = blogDao.getBlogById((long) 50);
        redisTemplate.opsForZSet().incrementScore("ViewCount","50",1);
        jay.setBlogViews(jay.getBlogViews()+1);
        blogDao.updateByPrimaryKeySelective(jay);
    }

    @Override
    public List<SimpleBlogListVO> getBlogListForViewCount() {
        List<SimpleBlogListVO> simpleBlogListVOS = new ArrayList<>();
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        Set viewCount = redisTemplate.opsForZSet().reverseRange("ViewCount", 0, 8);
        for(Object b : viewCount){
            String blog = b.toString();
            String[] blogg = blog.split(",");
            SimpleBlogListVO simpleBlogListVO = new SimpleBlogListVO();
            simpleBlogListVO.setBlogId(Long.valueOf(blogg[0]));
            simpleBlogListVO.setBlogTitle(blogg[1]);
            simpleBlogListVOS.add(simpleBlogListVO);
        }
        return simpleBlogListVOS;
    }

    @Override
    public List<SimpleBlogListVO> getBlogListForNewBlog() {
        List<SimpleBlogListVO> simpleBlogListVOS = new ArrayList<>();
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        List newBlog = redisTemplate.opsForList().range("NewBlog", 0, 8);
        for(Object b : newBlog){
            String blog = b.toString();
            String[] blogg = blog.split(",");
            SimpleBlogListVO simpleBlogListVO = new SimpleBlogListVO();
            simpleBlogListVO.setBlogId(Long.valueOf(blogg[0]));
            simpleBlogListVO.setBlogTitle(blogg[1]);
            simpleBlogListVOS.add(simpleBlogListVO);
        }
        return simpleBlogListVOS;
    }

    private List<BlogListVO> getBlogListVOsByBlogs(List<Blog> blogList) {
        List<BlogListVO> blogListVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(blogList)) {
            List<Integer> categoryIds = blogList.stream().map(Blog::getBlogCategoryId).collect(Collectors.toList());
            Map<Integer, String> blogCategoryMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(categoryIds)) {
                List<BlogCategory> blogCategories = blogCategoryDao.selectByCategoryIds(categoryIds);
                if (!CollectionUtils.isEmpty(blogCategories)) {
                    blogCategoryMap = blogCategories.stream().collect(Collectors.toMap(BlogCategory::getCategoryId, BlogCategory::getCategoryIcon, (key1, key2) -> key2));
                }
            }
            for (Blog blog : blogList) {
                BlogListVO blogListVO = new BlogListVO();
                BeanUtils.copyProperties(blog, blogListVO);
                if (blogCategoryMap.containsKey(blog.getBlogCategoryId())) {
                    blogListVO.setBlogCategoryIcon(blogCategoryMap.get(blog.getBlogCategoryId()));
                } else {
                    blogListVO.setBlogCategoryId(0);
                    blogListVO.setBlogCategoryName("默认分类");
                    blogListVO.setBlogCategoryIcon("/admin/dist/img/category/00.png");
                }
                blogListVOS.add(blogListVO);
            }
        }
        return blogListVOS;
    }

}
