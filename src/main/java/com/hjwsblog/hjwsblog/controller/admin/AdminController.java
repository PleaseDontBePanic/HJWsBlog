package com.hjwsblog.hjwsblog.controller.admin;

import com.hjwsblog.hjwsblog.entity.AdminUser;
import com.hjwsblog.hjwsblog.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 由于用来处理后台请映射，所以默认Mapping为admin开头
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminUserService adminUserService;
    @Resource
    private BlogService blogService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private LinkService linkService;
    @Resource
    private TagService tagService;
    @Resource
    private CommentService commentService;

    @GetMapping("/login")
    public String login(){
        return "admin/login";
    }

    @PostMapping(value = "/login")
    public String login(@RequestParam("userName")String userName,
                        @RequestParam("password")String password,
                        @RequestParam("verifyCode")String verifyCode,
                        HttpSession session){
//        首先验证输入内容的合法性
        if(StringUtils.isEmpty(verifyCode)){
            session.setAttribute("errorMsg", "验证码不能为空");
            return "admin/login";
        }
        if (org.springframework.util.StringUtils.isEmpty(userName) || org.springframework.util.StringUtils.isEmpty(password)) {
            session.setAttribute("errorMsg", "用户名或密码不能为空");
            return "admin/login";
        }
//        从session中获取当前验证码，若错误则返回login界面重新登录
        String kaptchaCode = session.getAttribute("verifyCode") + "";
        if (org.springframework.util.StringUtils.isEmpty(kaptchaCode) || !verifyCode.equals(kaptchaCode)) {
            session.setAttribute("errorMsg", "验证码错误");
            return "admin/login";
        }
//        将从后台数据库中查出的管理员用户、密码与传入的用户密码比较，若正确会返回AdminUser对象
        AdminUser login = adminUserService.login(userName, password);
        if(login != null){
//            将登录正确的用户名及id传入session中，供后续拦截器功能使用
            session.setAttribute("loginUser", login.getLogin_user_name());
            session.setAttribute("loginUserId", login.getLogin_user_id());
            return "redirect:/admin/index";//redirect:表示从项目静态资源根目录开始
        }else{
            session.setAttribute("errorMsg", "登陆失败");
            return "admin/login";
        }
    }

    /**
     * 对后台管理系统请求的处理，为对应类别数据数量进行查询并返回至前端显示
     * @param request
     * @return
     */
    @GetMapping({"","/","index","/index.html"})
    public String index(HttpServletRequest request){
//        path为index时对应的栏目会高亮显示
        request.setAttribute("path", "index");
        request.setAttribute("categoryCount", categoryService.getTotalCategories());//查找总分类数并为前端对应字段赋值
        request.setAttribute("blogCount", blogService.getTotalBlogs());
        request.setAttribute("linkCount", linkService.getTotalLinks());
        request.setAttribute("tagCount", tagService.getTotalTags());
        request.setAttribute("commentCount", commentService.getTotalComments());
        request.setAttribute("viewCount", blogService.getViewCount());
        return "admin/index";
    }

    /**
     * 处理修改用户信息的请求Controller
     * @param request
     * @return
     */
    @GetMapping("/profile")
    public String profile(HttpServletRequest request){
        request.setAttribute("path", "profile");//将profile标为选中
        request.setAttribute("loginUserName", request.getSession().getAttribute("loginUser"));//在前端界面打印当前登录用户
        return "admin/profile";
    }

    /**
     * 根据当前登录用户ID值修改当前登录用户名
     * @param request
     * @param loginUserName 欲修改后的用户名
     * @return
     */
    @PostMapping("/profile/name")
    @ResponseBody
    public String nameUpdate(HttpServletRequest request,
                             @RequestParam("loginUserName")String loginUserName){
        if(StringUtils.isEmpty(loginUserName)){
            return "参数不能为空";
        }
        int loginUserId = (int)request.getSession().getAttribute("loginUserId");//获取当前登录用户id值
        return adminUserService.updateName(loginUserId,loginUserName) ? "success" : "修改失败！";
    }

    /**
     * 修改当前登录用户密码,若修改成功则返回登录界面
     * @param request
     * @param originalPassword 原密码
     * @param newPassword 欲修改后的密码
     * @return
     */
    @PostMapping("/profile/password")
    @ResponseBody
    public String passwordUpdate(HttpServletRequest request,
                                 @RequestParam("originalPassword")String originalPassword,
                                 @RequestParam("newPassword") String newPassword){
        if (org.springframework.util.StringUtils.isEmpty(originalPassword) || org.springframework.util.StringUtils.isEmpty(newPassword)) {
            return "参数不能为空";
        }
        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
        if(adminUserService.updatePassword(loginUserId,originalPassword,newPassword)){
            //移出当前session中对应登录成功用户的信息，则用户会直接被拦截器弹回用户登录界面
            request.getSession().removeAttribute("loginUser");
            request.getSession().removeAttribute("loginUserId");
            request.getSession().removeAttribute("errorMsg");
            return "success";
        }else{
            return "修改失败！";
        }
    }

    /**
     * 移出session中用户信息的内容并返回login页面
     * @param request
     * @return
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request){
        request.getSession().removeAttribute("loginUser");
        request.getSession().removeAttribute("loginUserId");
        request.getSession().removeAttribute("errorMsg");
        return "admin/login";
    }
}
