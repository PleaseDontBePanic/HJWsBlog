package com.hjwsblog.hjwsblog.controller.blog;

import com.hjwsblog.hjwsblog.Dao.SubEmailsDao;
import com.hjwsblog.hjwsblog.controller.vo.BlogDetailVO;
import com.hjwsblog.hjwsblog.entity.Blog;
import com.hjwsblog.hjwsblog.entity.BlogComment;
import com.hjwsblog.hjwsblog.entity.BlogLink;
import com.hjwsblog.hjwsblog.entity.SubEmails;
import com.hjwsblog.hjwsblog.service.*;
import com.hjwsblog.hjwsblog.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class MyBlogController {
    //通过对theme字段名的选择达到主题的选择功能
//    public static String theme = "default";
    public static String theme = "yummy-jekyll";
//    public static String theme = "amaze";
    @Resource
    private BlogService blogService;
    @Resource
    private TagService tagService;
    @Resource
    private LinkService linkService;
    @Resource
    private CommentService commentService;
    @Resource
    private ConfigService configService;
    @Resource
    private CategoryService categoryService;
    @Autowired
    SubEmailsDao subEmailsDao;
    @Autowired
    MailService mailService;

    @GetMapping({"","/", "/index", "index.html"})
    public String index(HttpServletRequest request) {
        return this.page(request, 1);//即跳转到页码为1的页面
    }

    /***
     * 返回指定的page界面并为界面相关属性赋值
     * @param request
     * @param pageNum
     * @return
     */
    @GetMapping({"/page/{pageNum}"})
    public String page(HttpServletRequest request, @PathVariable("pageNum") int pageNum){
        PageResult blogs = blogService.getBlogsForIndexPage(pageNum);//以PageResult类返回当前页内的所有文章
        if (blogs == null) {
            return "error/error_404";
        }
        request.setAttribute("blogPageResult", blogs);//将查到的文章传入对应模块中
        request.setAttribute("newBlogs", blogService.getBlogListForNewBlog());//查询按发布时间排序的文章
        request.setAttribute("hotBlogs", blogService.getBlogListForViewCount());//查询按点击量排序的文章
        request.setAttribute("hotTags", tagService.getBlogTagCount());//查询按使用次数排序的Tag
        request.setAttribute("pageName", "首页");
        request.setAttribute("configurations", configService.getAllConfigs());//将配置从数据库中查出，配置相关配置项
        return "blog/" + theme + "/index";//返回对应主题的index界面
    }

    /**
     * 跳转至category界面的请求响应方法
     * @param request
     * @return
     */
    @GetMapping({"/categories"})
    public String categories(HttpServletRequest request) {
        request.setAttribute("hotTags", tagService.getBlogTagCountForIndex());//返回所有BlogTags
        request.setAttribute("categories", categoryService.getAllCategories());//返回所有Categories
        request.setAttribute("pageName", "分类页面");
        request.setAttribute("configurations", configService.getAllConfigs());//配置项
        return "blog/" + theme + "/category";
    }

    /**
     * 详情页
     * @return
     */
    @GetMapping({"/blog/{blogId}", "/article/{blogId}"})
    public String detail(HttpServletRequest request, @PathVariable("blogId") Long blogId, @RequestParam(value = "commentPage", required = false, defaultValue = "1") Integer commentPage) {
        if(blogId == 50){
            blogService.addJayView();
            return "music";
        }
        BlogDetailVO blogDetailVO = blogService.getBlogDetail(blogId);//获取Blog详细信息
        if (blogDetailVO != null) {//若Blog存在则为页面属性赋值
            request.setAttribute("blogDetailVO", blogDetailVO);
            //获取文章评论内容
            request.setAttribute("commentPageResult", commentService.getCommentPageByBlogIdAndPageNum(blogId, commentPage));
        }
        request.setAttribute("pageName", "详情");
        request.setAttribute("configurations", configService.getAllConfigs());
        return "blog/" + theme + "/detail";
    }

    /**
     * 标签列表页
     *
     * @return
     */
    @GetMapping({"/tag/{tagName}"})
    public String tag(HttpServletRequest request, @PathVariable("tagName") String tagName) {
        return tag(request, tagName, 1);
    }

    /**
     * 标签列表页
     *
     * @return
     */
    @GetMapping({"/tag/{tagName}/{page}"})
    public String tag(HttpServletRequest request, @PathVariable("tagName") String tagName, @PathVariable("page") Integer page) {
        PageResult blogPageResult = blogService.getBlogsPageByTag(tagName, page);
        request.setAttribute("blogPageResult", blogPageResult);
        request.setAttribute("pageName", "标签");
        request.setAttribute("pageUrl", "tag");
        request.setAttribute("keyword", tagName);
        request.setAttribute("newBlogs", blogService.getBlogListForNewBlog());
        request.setAttribute("hotBlogs", blogService.getBlogListForViewCount());
        request.setAttribute("hotTags", tagService.getBlogTagCount());//查询按使用次数排序的Tag
        request.setAttribute("configurations", configService.getAllConfigs());
        return "blog/" + theme + "/list";
    }

    /**
     * 分类列表页
     *
     * @return
     */
    @GetMapping({"/category/{categoryName}"})
    public String category(HttpServletRequest request, @PathVariable("categoryName") String categoryName) {
        return category(request, categoryName, 1);
    }

    /**
     * 分类列表页
     *
     * @return
     */
    @GetMapping({"/category/{categoryName}/{page}"})
    public String category(HttpServletRequest request, @PathVariable("categoryName") String categoryName, @PathVariable("page") Integer page) {
        PageResult blogPageResult = blogService.getBlogsPageByCategory(categoryName, page);
        request.setAttribute("blogPageResult", blogPageResult);
        request.setAttribute("pageName", "分类");
        request.setAttribute("pageUrl", "category");
        request.setAttribute("keyword", categoryName);
        request.setAttribute("newBlogs", blogService.getBlogListForNewBlog());
        request.setAttribute("hotBlogs", blogService.getBlogListForViewCount());
        request.setAttribute("hotTags", tagService.getBlogTagCount());
        request.setAttribute("configurations", configService.getAllConfigs());
        return "blog/" + theme + "/list";
    }

    @GetMapping({"/search/{keyword}/{page}"})
    public String search(HttpServletRequest request, @PathVariable("keyword") String keyword, @PathVariable("page") Integer page) {
        PageResult blogPageResult = blogService.getBlogsPageBySearch(keyword, page);
        request.setAttribute("blogPageResult", blogPageResult);
        request.setAttribute("pageName", "搜索");
        request.setAttribute("pageUrl", "search");
        request.setAttribute("keyword", keyword);
        request.setAttribute("newBlogs", blogService.getBlogListForNewBlog());
        request.setAttribute("hotBlogs", blogService.getBlogListForViewCount());
        request.setAttribute("hotTags", tagService.getBlogTagCount());
        request.setAttribute("configurations", configService.getAllConfigs());
        return "blog/" + theme + "/list";
    }
    /**
     * 搜索列表页
     *接收keyword参数并将其用search方法处理
     * @return
     */
    @GetMapping({"/search"})
    public String search(HttpServletRequest request, @RequestParam(value = "keyword",required = false ) String keyword) {
        return search(request, keyword, 1);
    }

    @GetMapping({"/sub"})
    public String sub(HttpServletRequest request, @RequestParam(value = "address",required = false ) String address){
        String Title = "订阅成功确认邮件";
        String context = "恭喜！！\r\n如果你收到此邮件说明你已经成功订阅本网站，感谢您对本网站的支持！！！此后将在每次更新文章时自动向您留下的邮箱发送信息，请注意查收\n" +
                " 此邮件为自动发送，请勿回复。\r\n-------HJW";
        mailService.sendMail(address,Title,context);
        SubEmails byAddress = subEmailsDao.getEmailByAddress(address);
        if(byAddress == null) subEmailsDao.insertEmail(address);
        return detail(request,(long)81,1);
    }
    /**
     * 友情链接页
     *
     * @return
     */
    @GetMapping({"/link"})
    public String link(HttpServletRequest request) {
        request.setAttribute("pageName", "友情链接");
        Map<Byte, List<BlogLink>> linkMap = linkService.getLinksForLinkPage();
        if (linkMap != null) {
            //判断友链类别并封装数据 0-友链 1-推荐 2-个人网站
            if (linkMap.containsKey((byte) 0)) {
                request.setAttribute("favoriteLinks", linkMap.get((byte) 0));
            }
            if (linkMap.containsKey((byte) 1)) {
                request.setAttribute("recommendLinks", linkMap.get((byte) 1));
            }
            if (linkMap.containsKey((byte) 2)) {
                request.setAttribute("personalLinks", linkMap.get((byte) 2));
            }
        }
        request.setAttribute("configurations", configService.getAllConfigs());
        return "blog/" + theme + "/link";
    }

    /**
     * 评论操作
     */
    @PostMapping(value = "/blog/comment")
    @ResponseBody
    public Result comment(HttpServletRequest request, HttpSession session,
                          @RequestParam Long blogId, @RequestParam String verifyCode,
                          @RequestParam String commentator, @RequestParam String email,
                          @RequestParam String websiteUrl, @RequestParam String commentBody){
        if (StringUtils.isEmpty(verifyCode)) {
            return ResultGenerator.genFailResult("验证码不能为空");
        }
        String kaptchaCode = session.getAttribute("verifyCode") + "";
        if (StringUtils.isEmpty(kaptchaCode)) {
            return ResultGenerator.genFailResult("非法请求");
        }
        if (!verifyCode.equals(kaptchaCode)) {
            return ResultGenerator.genFailResult("验证码错误");
        }
        String ref = request.getHeader("Referer");
        if (StringUtils.isEmpty(ref)) {
            return ResultGenerator.genFailResult("非法请求");
        }
        if (null == blogId || blogId < 0) {
            return ResultGenerator.genFailResult("非法请求");
        }
        if (StringUtils.isEmpty(commentator)) {
            return ResultGenerator.genFailResult("请输入称呼");
        }
        if (StringUtils.isEmpty(email)) {
            return ResultGenerator.genFailResult("请输入邮箱地址");
        }
        if (!PatternUtil.isEmail(email)) {
            return ResultGenerator.genFailResult("请输入正确的邮箱地址");
        }
        if (StringUtils.isEmpty(commentBody)) {
            return ResultGenerator.genFailResult("请输入评论内容");
        }
        if (commentBody.trim().length() > 200) {
            return ResultGenerator.genFailResult("评论内容过长");
        }
        BlogComment comment = new BlogComment();
        comment.setBlogId(blogId);
        comment.setCommentator(MyBlogUtils.cleanString(commentator));
        comment.setEmail(email);
        if (PatternUtil.isURL(websiteUrl)) {//验证其为URL格式
            comment.setWebsiteUrl(websiteUrl);
        }
        comment.setCommentBody(MyBlogUtils.cleanString(commentBody));
        comment.setReplyCreateTime(new Date());
        return ResultGenerator.genSuccessResult(commentService.addComment(comment));
    }

    /**
     * 关于页面 以及其他配置了subUrl的文章页
     *
     * @return
     */
    @GetMapping({"/{subUrl}"})
    public String detail(HttpServletRequest request, @PathVariable("subUrl") String subUrl) {
        BlogDetailVO detailVO = blogService.getBlogDetailBySubUrl(subUrl);
        if (detailVO != null) {
            request.setAttribute("blogDetailVO", detailVO);
            request.setAttribute("pageName", subUrl);
            request.setAttribute("configurations", configService.getAllConfigs());
            return "blog/" + theme + "/detail";
        } else {
            return "error/error_400";
        }
    }

    @GetMapping("/jay")
    public String jay(){
        blogService.addJayView();
        return "music";
    }
}
