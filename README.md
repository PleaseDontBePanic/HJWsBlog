

# MyBlog项目开发流程及知识点

一个个人博客项目
完成链接为https://hjwzqy.online
欢迎访问、订阅
关于项目的任何问题都可以联系邮箱13633457537@163.com
以下是此项目的步骤流程

>HJW的博客：
>[HJWs Blog](www.hjwzqy.online:15498)

## 项目中依赖的引入及配置测试

用IDEA直接创建SpringBoot工程，修改pom依赖中Spring版本为2.1.0.RELEASE，方便之后使用。

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.1.0.RELEASE</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>
```

对Thymeaf的依赖引入：

```xml
<!--        引入thymeleaf依赖-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
```

对Thymeleaf的测试，创建模板文件：

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>thymeleaf demo</title>
</head>
<body>
<p>description 字段值为：</p>
<p th:text="${description}">这里显示description字段值</p>
</body>
</html>
```

```java
@GetMapping("/thymeleaf")
public String hello(HttpServletRequest request ,@RequestParam(value = "description",required = false , defaultValue = "thymeleaf") String description){
    request.setAttribute("description","传值为:"+description);
    return "thymeleaf";
}
```

![](https://s1.ax1x.com/2022/04/14/LlOgxA.png)

可看到通过测试使用Thymeleaf可以很好地实现将静态html页面中特定变量赋值的作用，这将在本项目中发挥重要作用

对jdbc及数据库链接的配置：

```xml
<!--        链接数据库配置-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.27</version>
            <scope>runtime</scope>
        </dependency>
```

对ByBatis的整合：

```xml
<!--        整合MyBatis-->
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>2.1.0</version>
        </dependency>
```

验证码部分模块的依赖引入：

```xml
<!--        验证码-->
        <dependency>
            <groupId>com.github.penggle</groupId>
            <artifactId>kaptcha</artifactId>
            <version>2.3.2</version>
        </dependency>
```

## Controller中admin部分各方法的逻辑流程

在java.com.hjwsbloghjw.blog下新建Controller库，（注意一定要和main函数一个级别，否则不会识别到Controller方法请求）

![](https://s1.ax1x.com/2022/04/14/Ll5RKS.png)

### 新建admin目录用来存放admin（即后台管理界面）的Controller类用来处理请求、映射

```java
/**
 * 由于用来处理后台请映射，所以默认Mapping为admin开头
 */
@Controller
@RequestMapping("/admin")
public class AdminController {}
```

### 对一些常用注解的介绍：

`@Controller：标注此类为控制类，用于处理反射及请求`

`@RequestMapping:用于接收程序前端、Controller类的请求，可选请求类型，如get、post等`
`@Getmapping：模式为get的请求；@Postmapping：模式为post的请求`

`@ResponseBody：该注解用于直接返回User对象（当返回POJO对象时，会默认转为json格式数据进行响应）。`

举例：如不加此注解，通常返回的是字符串，指向程序要跳转的页面的地址或请求，如：

```java
@GetMapping("/login")
public String login(){
    return "admin/login";
}
```

未加@ResponseBody注解，就会跳向静态资源下admin/login.html界面

但若加此注解就会返回具体字段对象（String），结合一些逻辑实现功能，如生成警示框反馈修改结果等，如下：

```java
/**
 * 修改当前登录用户名
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
    int loginUserId = (int)request.getSession().getAttribute("loginUserId");
    return adminUserService.updateName(loginUserId,loginUserName) ? "success" : "修改失败！";
}
```

其中还使用到了很多@Resouce及@Autowired语法，这部分内容较多，会另开一篇文章记录

用@Resource注解引入Service接口

```java
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
```

### 对url /login请求（用来登录后台管理系统的地址）处理

```java
@GetMapping("/login")
public String login(){
    return "admin/login";
}
```

直接返回静态变量中的login.html，值得注意的是本项目中有拦截器模块设置，拦截除login请求及静态资源外的admin开头的所有请求（除非Session中存有登陆成功信息），后续介绍。

收到请求后服务器会返回login.html资源，其中三行input栏为分别输入userName、password、验证码内容，提取代码：

```html
<input type="text" id="userName" name="userName" class="form-control" placeholder="请输入账号"
required="true">
<input type="password" id="password" name="password" class="form-control" placeholder="请输入密码"
required="true">
<input type="text" class="form-control" name="verifyCode" placeholder="请输入验证码" required="true">
```

其中存在验证码的生成（随网页加载会生成一次，单击验证码图片同样会进行刷新）模块，单击图片会生成/common/kaptha请求：

```html
<img alt="单击图片刷新！" class="pointer" th:src="@{/common/kaptcha}"
     onclick="this.src='/common/kaptcha?d='+new Date()*1">
```

### 验证码模块功能实现

首先要对模块依赖进行导入，之前提及过，不再赘述

在controller目录下新建common目录用来进行对请求的处理及模块的配置

![](https://s1.ax1x.com/2022/04/14/L1FaM8.png)

```java
@Controller
public class CommonController {
//    引入所需对象
    @Autowired
    private DefaultKaptcha captchaProducer;
    /**
     * 对/common/kaptcha请求处理
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws Exception
     */
    @GetMapping("/common/kaptcha")
    public void defaultKaptcha(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        byte[] captchaOutputStream = null; //用于存储、输出图片的字节流
        ByteArrayOutputStream imgOutputStream = new ByteArrayOutputStream();
        try {
            //生产验证码字符串并保存到session中
            String verifyCode = captchaProducer.createText(); //生成验证码（根据配置文件格式）
            httpServletRequest.getSession().setAttribute("verifyCode", verifyCode);//将验证码写入到session中用于后续登录的验证
            BufferedImage challenge = captchaProducer.createImage(verifyCode);//生成验证码图片，来传到前端显示
            ImageIO.write(challenge, "jpg", imgOutputStream);
        } catch (IllegalArgumentException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        captchaOutputStream = imgOutputStream.toByteArray();
        httpServletResponse.setHeader("Cache-Control", "no-store");
        httpServletResponse.setHeader("Pragma", "no-cache");
        httpServletResponse.setDateHeader("Expires", 0);
        httpServletResponse.setContentType("image/jpeg");
        ServletOutputStream responseOutputStream = httpServletResponse.getOutputStream();
        responseOutputStream.write(captchaOutputStream);
        responseOutputStream.flush();
        responseOutputStream.close();
    }
}
```

此Controller的主要作用既是生成随机验证码并将此验证码传入session中，且返回生成的与验证码对应的图片至前端显示

```java
//验证码配置类
@Component
public class KaptchaConfig {
    @Bean
    public DefaultKaptcha getDefaultKaptcha(){
        com.google.code.kaptcha.impl.DefaultKaptcha defaultKaptcha = new com.google.code.kaptcha.impl.DefaultKaptcha();
        Properties properties = new Properties();
        properties.put("kaptcha.border", "no");
        properties.put("kaptcha.textproducer.font.color", "black");
        properties.put("kaptcha.image.width", "150");
        properties.put("kaptcha.image.height", "40");
        properties.put("kaptcha.textproducer.font.size", "30");
        properties.put("kaptcha.session.key", "verifyCode");
        properties.put("kaptcha.textproducer.char.space", "5");
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
```

### 对post方法的login请求的处理

在login.html中将信息输入后点击登录按钮会提交post请求，指向login

```html
<button type="submit" class="btn btn-primary btn-block btn-flat">登录
</button>
```

在AdminController中编写方法对其进行处理

```java
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
```

可以看到方法中调用了adminUserService对象中的login方法，此方法主要是通过将前端输入的userName、password信息与后台中存储的User信息进行比对，若后台中存在此管理用户则放行，在session中输入对应的用户信息，以通过拦截器访问后台管理页面。

```java
/**
 * 与传入信息进行一一比较，若比对成功则返回对应AdminUser对象，失败则返回null
 * @param userName 传入用户名
 * @param password 传入用户密码
 * @return
 */
@Override
public AdminUser login(String userName, String password) {
    List<AdminUser> allUsers = adminUserDao.findAllUsers();//获取所有管理用户信息
    for(AdminUser user : allUsers){
        if(userName.equals(user.getLogin_user_name()) && password.equals(user.getLogin_user_password())){
            return user;
        }
    }
    return null;
}
```

AdminUserDao中findAllUsers()对应的xml sql语句：

```sql
<select id="findAllUsers" resultMap="AdminUserResult">
    select * from admin_user
</select>
```

### 后台管理界面首页

```java
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
        return "admin/index";
    }
```

通过对前端页面${path}字段的赋值及三元运算符来实现对应标签选中功能，前端对应部分代码如下：

```html
<a th:href="@{/admin/index}" th:class="${path}=='index'?'nav-link active':'nav-link'">
                        <i class="nav-icon fa fa-dashboard"></i>
                        <p>
                            Dashboard
                        </p>
                    </a>
```

<img src="https://s1.ax1x.com/2022/04/14/L1BBm6.png" style="zoom:50%;" />

而对前端分类内容数量的赋值部分代码如：

```html
<!-- small box -->
<div class="small-box bg-warning">
    <div class="inner">
        <h3 th:text="${categoryCount}">65</h3> //为categoryCount赋值后显示
        <p>分类数量</p>
    </div>
    <div class="icon">
        <i class="fa fa-bookmark"></i>
    </div>
    <a th:href="@{/admin/categories}" class="small-box-footer">More info <i
            class="fa fa-arrow-circle-right"></i></a>
</div>
```

![](https://s1.ax1x.com/2022/04/14/L1DL5D.png)

对应功能实现图如上

各部分调用了对应的Service对象中的getTotal方法，返回从后台数据库中查到的数据总数，以BlogTagDao对应的MyBatis的xml文件为例，sql如下：

```sql
<select id="getTotalTags"  resultType="int">
    select count(*) from tag
    where is_deleted=0
</select>
```

### 修改用户信息页面

```java
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
```

<img src="https://s1.ax1x.com/2022/04/14/L14Xin.png" style="zoom:40%;" />

效果如上

对用户名及密码进行修改对应方法：

```java
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
```

```java
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
```

### 安全退出模块

```java
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
```

### 拦截器模块

拦截器模块主要由两部分构成，即拦截器的配置模块及设置部分，配置模块如下：

```java
@Configuration
public class MyBlogWebMvcConfigurer {
    @Autowired
    private AdminLoginInterceptor adminLoginInterceptor;

    public void addInterceptors(InterceptorRegistry registry) {
        // 添加一个拦截器，拦截以/admin为前缀的url路径，但不拦截登录界面及对静态变量的访问
        registry.addInterceptor(adminLoginInterceptor).addPathPatterns("/admin/**").excludePathPatterns("/admin/login").excludePathPatterns("/admin/dist/**").excludePathPatterns("/admin/plugins/**");
    }
}
```

拦截器的设置部分为一对HandlerInterceptor接口的实现类，通过对其preHandle方法的逻辑重写来实现拦截功能，主要逻辑为：返回true则不予拦截，false则进行拦截：

```java
@Component
public class AdminLoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        String requestServletPath = request.getServletPath();
//        若用户欲访问/admin开头的页面而此时session中又没有写入用户名等信息则视为此前没有进行过登录，需返回登录界面重新登陆
        if (requestServletPath.startsWith("/admin") && null == request.getSession().getAttribute("loginUser")) {
            request.getSession().setAttribute("errorMsg", "请重新登陆");
            response.sendRedirect(request.getContextPath() + "/admin/login");//设置拦截后的返回地址
            return false;
        } else {
            request.getSession().removeAttribute("errorMsg");
            return true;
        }
    }

}
```

## Controller中后台管理系统各部分方法的逻辑流程

### CategoryController各方法

首先还是将category栏设为选中样式

```java
/**
 * 对访问category的请求将path设为categories（前端变为选中格式）
 * @param request
 * @return
 */
@GetMapping("/categories")
public String categories(HttpServletRequest request){
    request.setAttribute("path","categories");
    return "admin/category";
}
```

首先会跳转至显示category的初始页面，将按页显示所有分组，此时前端会向后端传入/admin/categories/list的URL请求，且会传入一Map格式的数据，其中包括了要查询的页数及每页的数据个数，经检查数据非空后将其包装为一PageQueryUtil对象，并生成对应的起始地址，其中pageUtil为LinkedHashMap的子类，在查询对应页数结果后打包为一个json格式的数据（Result）返回给前端ajex渲染

```java
/**
 * 首先会生成一个列表格式来显示所有的分类
 * 前端传入对应的列、行的信息，后台相应的得出数据返回显示
 * @param params
 * @return
 */
@GetMapping("/categories/list")
@ResponseBody
public Result list(@RequestParam Map<String, Object> params){
    if (StringUtils.isEmpty(params.get("page")) || StringUtils.isEmpty(params.get("limit"))) {
        return ResultGenerator.genFailResult("参数异常！");
    }
    PageQueryUtil pageUtil = new PageQueryUtil(params);//包装为PageQueryUtil对象
    //调用ResultGenerator中的方法生成json数据返回至前端ajex渲染
    return ResultGenerator.genSuccessResult(categoryService.getBlogCategoryPage(pageUtil));
}
```

```java
public PageQueryUtil(Map<String, Object> params) {
    this.putAll(params);
    //分页参数
    this.page = Integer.parseInt(params.get("page").toString());
    this.limit = Integer.parseInt(params.get("limit").toString());
    this.put("start", (page - 1) * limit);
    this.put("page", page);
    this.put("limit", limit);
}
```

随后以pageUtil对象为参传入blogCategoryDao中的findCategoryList方法，返回CategoryList，以category_rank、create_time降序

```sql
<select id="findCategoryList" parameterType="Map" resultMap="BlogCategoryResult">
    select * from blog_category
    where is_deleted=0
    order by category_rank desc, create_time desc
    <if test="start!=null and limit!=null">
        limit #{start},#{limit}
    </if>
</select>
```

随后将查询到的数据封装到一个PageResult对象中

```java
@Override
public PageResult getBlogCategoryPage(PageQueryUtil pageUtil) {
    List<BlogCategory> categoryList = blogCategoryDao.findCategoryList(pageUtil);
    int total = blogCategoryDao.getTotalCategories();
    PageResult pageResult = new PageResult(categoryList, total, pageUtil.getLimit(), pageUtil.getPage());
    return pageResult;
}
```

其中生成Result的对应方法：

```java
public static Result genSuccessResult(Object data) {
    Result result = new Result();
    result.setResultCode(RESULT_CODE_SUCCESS);
    result.setMessage(DEFAULT_SUCCESS_MESSAGE);
    result.setData(data);
    return result;
}
```

可以看到其主要是返回ResultCode等信息，并将Result对象中的泛型参数data赋值为相应的结果（在本例中为包含Category结果的List与总数量、每页数量、当前页的信息）

前端读取json格式数据的代码如下，读取后js文件会将json中的数据自动渲染至页面上

```js
jsonReader: {
    root: "data.list",
    page: "data.currPage",
    total: "data.totalPage",
    records: "data.totalCount"
}
```

新增category的方法：

```java
/**
 * 新增分类模式的代码
 * @param categoryName
 * @param categoryIcon
 * @return
 */
@PostMapping("/categories/save")
@ResponseBody
public Result save(@RequestParam("categoryName") String categoryName,
                   @RequestParam("categoryIcon") String categoryIcon){
    if (StringUtils.isEmpty(categoryName)) {
        return ResultGenerator.genFailResult("请输入分类名称！");
    }
    if (StringUtils.isEmpty(categoryIcon)) {
        return ResultGenerator.genFailResult("请选择分类图标！");
    }
    if(categoryService.saveCategory(categoryName, categoryIcon)){
        return ResultGenerator.genSuccessResult();
    }else{
        return ResultGenerator.genFailResult("分类名称重复");
    }
}
```

用户将把从选择模态框中输入、选择的信息传入后台进行分类的添加操作，添加成功返回success json字串。

<img src="https://s1.ax1x.com/2022/04/17/LNdDRU.png" style="zoom:40%;" />

```java
@Override
public boolean saveCategory(String categoryName, String categoryIcon) {
    //先根据新增的分类名查找有无已存在的同名分类，若无则继续添加
    BlogCategory selected = blogCategoryDao.selectByCategoryName(categoryName);
    if(selected == null){
        BlogCategory blogCategory = new BlogCategory();
        blogCategory.setCategoryName(categoryName);
        blogCategory.setCategoryIcon(categoryIcon);
        return blogCategoryDao.insertSelective(blogCategory) > 0;
    }
    return false;
}
```

对已有分类进行图标或名称的修改及删除分类的方法

```java
/**
 * 根据所选的已有分类对后台数据中的分类名、图标进行修改
 * @param categoryId
 * @param categoryName
 * @param categoryIcon
 * @return
 */
@PostMapping("/categories/update")
@ResponseBody
public Result update(@RequestParam("categoryId") Integer categoryId,
                     @RequestParam("categoryName") String categoryName,
                     @RequestParam("categoryIcon") String categoryIcon){
    if (StringUtils.isEmpty(categoryName)) {
        return ResultGenerator.genFailResult("请输入分类名称！");
    }
    if (StringUtils.isEmpty(categoryIcon)) {
        return ResultGenerator.genFailResult("请选择分类图标！");
    }
    if(categoryService.updateCategory(categoryId, categoryName, categoryIcon)){
        return ResultGenerator.genSuccessResult();
    }else{
        return ResultGenerator.genFailResult("分类名称重复");
    }
}

/**
 * 删除分类方法，将数据的is_deleted置为1
 * @param ids
 * @return
 */
@PostMapping("/categories/delete")
@ResponseBody
public Result delete(@RequestBody Integer[] ids){
    if (ids.length < 1) {
        return ResultGenerator.genFailResult("参数异常！");
    }
    if(categoryService.deleteBatch(ids)){
        return ResultGenerator.genSuccessResult();
    }else{
        return ResultGenerator.genFailResult("删除失败");
    }
}
```

### TagController各方法

<img src="https://s1.ax1x.com/2022/04/17/LUFgTP.png" style="zoom:30%;" />

总体逻辑、流程与category一致分为按页显示Tags、增删改Tags方法。值得注意的是在删除标签时如果欲删除的标签中存在正在被使用的标签则阻止删除并返回警告，其中Blog与Tag关系表设计如下：

```sql
CREATE TABLE `blog_tag_relation` (
  `relation_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '关系表id',
  `blog_id` BIGINT(20) NOT NULL COMMENT '博客id',
  `tag_id` INT(11) NOT NULL COMMENT '标签id',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`relation_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;
```

删除方法：

```java
/**
 * 删除所选列表中的Tags，若有关联则阻止删除
 * @param ids
 * @return
 */
@PostMapping("/tags/delete")
@ResponseBody
public Result delete(@RequestBody Integer[] ids){
    if (ids.length < 1) {
        return ResultGenerator.genFailResult("参数异常！");
    }
    if (tagService.deleteBatch(ids)) {
        return ResultGenerator.genSuccessResult();
    } else {
        return ResultGenerator.genFailResult("有关联数据请勿强行删除");
    }
}
```

### LinkController各方法

用于显示各友链，仍旧是沿用了之前的逻辑思路，无显著变化

<img src="https://s1.ax1x.com/2022/04/17/LUAZ5V.png" style="zoom:33%;" />

### ConfigurationController各方法

用于显示及修改一些网站的配置，略微值得注意的是在接收/configurations的URL请求时会现将所有配置加载到前端显示：

```java
/**
 * 返回当前所有配置
 * @param request
 * @return
 */
@GetMapping("/configurations")
public String list(HttpServletRequest request) {
    request.setAttribute("path", "configurations");
    request.setAttribute("configurations", configService.getAllConfigs());
    return "admin/configuration";
}
```

后续对相关配置的修改不再赘述

### CommentController各方法

主要为显示、审核、回复在文章下的评论的页面，主界面如下：

<img src="https://s1.ax1x.com/2022/04/17/LUVjER.png" style="zoom:33%;" />

与其余页面不同的是其对评论的审核与回复功能，但大体逻辑上仍类似，均为经前台页面的选择后传入id数组，后端接收后将数组中的评论审核通过并返回json串通知前端审核结果

```java
/**
 * 将ids中的id评论审核通过
 * @param ids
 * @return
 */
@PostMapping("/comments/checkDone")
@ResponseBody
public Result checkDone(@RequestBody Integer[] ids){
    if (ids.length < 1) {
        return ResultGenerator.genFailResult("参数异常！");
    }
    if (commentService.checkDone(ids)) {
        return ResultGenerator.genSuccessResult();
    } else {
        return ResultGenerator.genFailResult("审核失败");
    }
}
```

而回复评论则首先要求评论处于审核通过状态下，后便可将接受到前端传入的回复信息及回复时间写入对应的数据库中：

```java
@Override
public Boolean reply(Long commentId, String replyBody) {
    BlogComment blogComment = blogCommentDao.selectByPrimaryKey(commentId);
    //blogComment不为空且状态为已审核，则继续后续操作
    if (blogComment != null && blogComment.getCommentStatus().intValue() == 1){
        blogComment.setReplyBody(replyBody);
        blogComment.setReplyCreateTime(new Date());
        return blogCommentDao.updateByPrimaryKeySelective(blogComment) > 0;
    }
    return false;
}
```

## 后台撰写文章部分的逻辑流程

本部分主要为博客文章的浏览与修改删除以及撰写新文章的部分以及前端关于文章编辑部分的一些插件的引用，下面也将分三部分介绍

### 历史文章的浏览与修改删除部分

这部分与之前几部分总体逻辑并无区别

<img src="https://s1.ax1x.com/2022/04/17/LaSX8J.png" style="zoom:33%;" />

```java
/**
 * 对指定id博客的修改界面
 * @param request
 * @param blogId
 * @return
 */
@GetMapping("/blogs/edit/{blogId}")
public String edit(HttpServletRequest request, @PathVariable("blogId") Long blogId) {
    request.setAttribute("path", "edit");//进入edit界面，设置edit为选中图样
    Blog blog = blogService.getBlogById(blogId);//获取待修改Blog的信息
    if (blog == null) {
        return "error/error_400";
    }
    request.setAttribute("blog", blog);//将待修改Blog信息填入栏中
    request.setAttribute("categories", categoryService.getAllCategories());
    return "admin/edit";
}
```

对文章的修改对应的Controller：

```java
@PostMapping("/blogs/update")
@ResponseBody
public Result update(@RequestParam("blogId") Long blogId,
                     @RequestParam("blogTitle") String blogTitle,
                     @RequestParam(name = "blogSubUrl", required = false) String blogSubUrl,
                     @RequestParam("blogCategoryId") Integer blogCategoryId,
                     @RequestParam("blogTags") String blogTags,
                     @RequestParam("blogContent") String blogContent,
                     @RequestParam("blogCoverImage") String blogCoverImage,
                     @RequestParam("blogStatus") Byte blogStatus,
                     @RequestParam("enableComment") Byte enableComment) {
    if (StringUtils.isEmpty(blogTitle)) {
        return ResultGenerator.genFailResult("请输入文章标题");
    }
    if (blogTitle.trim().length() > 150) {
        return ResultGenerator.genFailResult("标题过长");
    }
    if (StringUtils.isEmpty(blogTags)) {
        return ResultGenerator.genFailResult("请输入文章标签");
    }
    if (blogTags.trim().length() > 150) {
        return ResultGenerator.genFailResult("标签过长");
    }
    if (blogSubUrl.trim().length() > 150) {
        return ResultGenerator.genFailResult("路径过长");
    }
    if (StringUtils.isEmpty(blogContent)) {
        return ResultGenerator.genFailResult("请输入文章内容");
    }
    if (blogContent.trim().length() > 100000) {
        return ResultGenerator.genFailResult("文章内容过长");
    }
    if (StringUtils.isEmpty(blogCoverImage)) {
        return ResultGenerator.genFailResult("封面图不能为空");
    }
    Blog blog = new Blog();
    blog.setBlogId(blogId);
    blog.setBlogTitle(blogTitle);
    blog.setBlogSubUrl(blogSubUrl);
    blog.setBlogCategoryId(blogCategoryId);
    blog.setBlogTags(blogTags);
    blog.setBlogContent(blogContent);
    blog.setBlogCoverImage(blogCoverImage);
    blog.setBlogStatus(blogStatus);
    blog.setEnableComment(enableComment);
    String updateBlogResult = blogService.updateBlog(blog);
    if ("success".equals(updateBlogResult)) {
        return ResultGenerator.genSuccessResult("修改成功");
    } else {
        return ResultGenerator.genFailResult(updateBlogResult);
    }
}
```

updateBlog方法相比新增Blog方法主要多了一个将之前的Blog-Tags关系删除的步骤，其余思路基本一致，代码中的注释已相当详尽

```java
	@Override
    @Transactional //其本质是对方法前后进行拦截，然后在目标方法开始之前创建或者加入一个事务，在执行完目标方法之后根据执行情况提交或者回滚事务。
    public String updateBlog(Blog blog) {
        Blog blogForUpdate = blogDao.getBlogById(blog.getBlogId());
        if (blogForUpdate == null) {
            return "数据不存在";
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
```

### 新文章的编写

文章编辑界面Controller

```java
/**
 *进入文章编辑界面，并查询所有已有category内容供文章分类选择
 * @param request
 * @return
 */
@GetMapping("/blogs/edit")
public String edit(HttpServletRequest request) {
    request.setAttribute("path", "edit");
    request.setAttribute("categories", categoryService.getAllCategories());
    return "admin/edit";
}
```

对新文章的保存请求进行处理controller

```java
@PostMapping("/blogs/save")
@ResponseBody
public Result save(@RequestParam("blogTitle") String blogTitle,
                   @RequestParam(name = "blogSubUrl", required = false) String blogSubUrl,
                   @RequestParam("blogCategoryId") Integer blogCategoryId,
                   @RequestParam("blogTags") String blogTags,
                   @RequestParam("blogContent") String blogContent,
                   @RequestParam("blogCoverImage") String blogCoverImage,
                   @RequestParam("blogStatus") Byte blogStatus,
                   @RequestParam("enableComment") Byte enableComment){
    if (StringUtils.isEmpty(blogTitle)) {
        return ResultGenerator.genFailResult("请输入文章标题");
    }
    if (blogTitle.trim().length() > 150) {
        return ResultGenerator.genFailResult("标题过长");
    }
    if (StringUtils.isEmpty(blogTags)) {
        return ResultGenerator.genFailResult("请输入文章标签");
    }
    if (blogTags.trim().length() > 150) {
        return ResultGenerator.genFailResult("标签过长");
    }
    if (blogSubUrl.trim().length() > 150) {
        return ResultGenerator.genFailResult("路径过长");
    }
    if (StringUtils.isEmpty(blogContent)) {
        return ResultGenerator.genFailResult("请输入文章内容");
    }
    if (blogContent.trim().length() > 100000) {
        return ResultGenerator.genFailResult("文章内容过长");
    }
    if (StringUtils.isEmpty(blogCoverImage)) {
        return ResultGenerator.genFailResult("封面图不能为空");
    }
    Blog blog = new Blog();
    blog.setBlogTitle(blogTitle);
    blog.setBlogSubUrl(blogSubUrl);
    blog.setBlogCategoryId(blogCategoryId);
    blog.setBlogTags(blogTags);
    blog.setBlogContent(blogContent);
    blog.setBlogCoverImage(blogCoverImage);
    blog.setBlogStatus(blogStatus);
    blog.setEnableComment(enableComment);
    String res = blogService.saveBlog(blog);
    if(res.equals("success")){
        return ResultGenerator.genSuccessResult("添加成功");
    }else{
        return ResultGenerator.genFailResult(res);
    }
}
```

```java
@Override
    @Transactional
    public String saveBlog(Blog blog) {
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
            for(BlogTagRelation relation : relas){
                //将所有表都存入数据库中
                if(blogTagRelationDao.bathInsert(relation) <= 0){
                    return "保存失败";
                }
            }
        }
        return "success";
    }
```

### 编辑界面MarkDown语法富文本编辑器的引用及一些功能、插件的引用

编辑界面对富文本编辑器及插件的引用主要是现在edit.html中声明对js中方法的调用，后再相关js中完善具体方法，主要使用的是EditorMD插件

<img src="https://s1.ax1x.com/2022/04/18/LwKrSs.png" style="zoom:33%;" />

在edit.html中引入富文本编辑器：

```html
<!--引入富文本编辑器，在js中的id值为blog-editormd-->
<div class="form-group" id="blog-editormd">
    <textarea style="display:none;"
              th:utext="${blog!=null and blog.blogContent !=null}?${blog.blogContent}: ''"></textarea>
</div>
```

js文件中富文本编辑器的配置

```js
$(function () {
    blogEditor = editormd("blog-editormd", {
        width: "100%",
        height: 640,
        syncScrolling: "single",
        path: "/admin/plugins/editormd/lib/",
        toolbarModes: 'full',
        /**图片上传配置*/
        imageUpload: true,
        imageFormats: ["jpg", "jpeg", "gif", "png", "bmp", "webp"], //图片上传格式
        imageUploadURL: "/admin/blogs/md/uploadfile",
        onload: function (obj) { //上传成功之后的回调
        }
    });
```

编辑界面Tags编写配置：

```html
<input type="text" class="form-control" id="blogTags" name="blogTags"
       placeholder="请输入文章标签"
       th:value="${blog!=null and blog.blogTags!=null }?${blog.blogTags}: ''"
       style="width: 100%;">
```

js中相应的方法：

```js
$('#blogTags').tagsInput({
    width: '100%',
    height: '38px',
    defaultText: '文章标签'
});
```

edit.html的上传封面功能对应的调用

```html
<button class="btn btn-info" style="margin-bottom: 5px;" id="uploadCoverImage">
    <i class="fa fa-picture-o"></i>&nbsp;上传封面
</button>
```

js中相应的方法：

```js
new AjaxUpload('#uploadCoverImage', {
    action: '/admin/upload/file',
    name: 'file',
    autoSubmit: true,
    responseType: "json",
    onSubmit: function (file, extension) {
        if (!(extension && /^(jpg|jpeg|png|gif)$/.test(extension.toLowerCase()))) {
            alert('只支持jpg、png、gif格式的文件！');
            return false;
        }
    },
    onComplete: function (file, r) {
        if (r != null && r.resultCode == 200) {
            $("#blogCoverImage").attr("src", r.data);
            $("#blogCoverImage").attr("style", "width: 128px;height: 128px;display:block;");
            return false;
        } else {
            alert("error");
        }
    }
});
```

edit.html的随机封面功能对应的调用

```html
<button class="btn btn-secondary" style="margin-bottom: 5px;"
        id="randomCoverImage"><i
        class="fa fa-random"></i>&nbsp;随机封面
</button>
```

js中相应的方法：

```js
/**
 * 随机封面功能
 */
$('#randomCoverImage').click(function () {
    var rand = parseInt(Math.random() * 40 + 1);
    $("#blogCoverImage").attr("src", '/admin/dist/img/rand/' + rand + ".jpg");
    $("#blogCoverImage").attr("style", "width:160px ;height: 120px;display:block;");
});
```

 ## 博客显示端界面代码的编写与逻辑流程

本部分主要介绍博客前台展示界面各功能的实现逻辑流程，如主题的选择、对文章及标签的排序、主页面的显示渲染等内容

### 主题的选择

```java
//通过对theme字段名的选择达到主题的选择功能
//    public static String theme = "default";
    public static String theme = "yummy-jekyll";
//    public static String theme = "amaze";
```

以主界面映射Controller为例，最后返回的页面会包含theme字段

```java
/***
 * 返回index界面并为界面相关属性赋值
 * @param request
 * @param pageNum
 * @return
 */
@GetMapping({"/page/{pageNum}"})
public String page(HttpServletRequest request, @PathVariable("pageNum") int pageNum){
    PageResult blogs = blogService.getBlogsForIndexPage(pageNum);
    if (blogs == null) {
        return "error/error_404";
    }
    request.setAttribute("blogPageResult", blogs);
    request.setAttribute("newBlogs", blogService.getBlogListForIndexPage(1));
    request.setAttribute("hotBlogs", blogService.getBlogListForIndexPage(0));
    request.setAttribute("hotTags", tagService.getBlogTagCountForIndex());
    request.setAttribute("pageName", "首页");
    request.setAttribute("configurations", configService.getAllConfigs());
    return "blog/" + theme + "/index";//返回指定主题的index页面
}
```

静态文件中各主题的html文件目录如下:

<img src="https://s1.ax1x.com/2022/04/18/Lw3yZt.png" style="zoom:67%;" />

### 博客首页各功能的编写逻辑

主页界面预览：

<img src="https://s1.ax1x.com/2022/04/18/Lw8Uwq.png" style="zoom:43%;" />

访问博客界面的URL请求处理，可以看到首页就是页码为1 的页面：

```java
@GetMapping({"","/", "/index", "index.html"})
public String index(HttpServletRequest request) {
    return this.page(request, 1);//即跳转到页码为1的页面
}
```

对页面及首页（即页码为1的页面）请求的处理：

```java
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
    request.setAttribute("newBlogs", blogService.getBlogListForIndexPage(1));//查询按发布时间排序的文章
    request.setAttribute("hotBlogs", blogService.getBlogListForIndexPage(0));//查询按点击量排序的文章
    request.setAttribute("hotTags", tagService.getBlogTagCountForIndex());//查询按使用次数排序的文章
    request.setAttribute("pageName", "首页");
    request.setAttribute("configurations", configService.getAllConfigs());//将配置从数据库中查出，配置相关配置项
    return "blog/" + theme + "/index";//返回对应主题的index界面
}
```

下面将各方法逻辑逐一讲解：

#### 读取每页对应文章的方法逻辑

首先看前端界面对读取文章方法返回值的处理：

```html
<th:block th:if="${null != blogPageResult}">
    <th:block th:each="blog,iterStat : ${blogPageResult.list}">
        <li class="post-list-item">
            <h3 class="post-list-title">
                <a class="hvr-underline-from-center" th:href="@{'/blog/' + ${blog.blogId}}">
                    <th:block th:text="${blog.blogTitle}"></th:block>
                </a>
            </h3>
            <p class="post-list-cover-img">
                <a th:href="@{'/blog/' + ${blog.blogId}}"> <img th:src="@{${blog.blogCoverImage}}"
                                                                alt=""></a>
            </p>
            <p class="post-list-meta">
                <span class="octicon octicon-calendar"
                      th:text="${#dates.format(blog.createTime, 'yyyy-MM-dd')}"></span>
            </p>
        </li>
    </th:block>
</th:block>
```

可以看到其将blogPageResult中List的每一个对象都输出其Title、链接、封面图信息

因此返回的List对象也应包含这些内容，而不需所有内容（如文章全文就是明显的冗余信息，无需传递至前端）

```java
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
```

其中BlogDao中findBlogList方法xml配置：

```sql
<select id="findBlogList" resultMap="BaseResultMap" parameterType="Map">
    select * from blog
    where is_deleted=0
    <if test="keyword!=null">
        AND (blog_title like CONCAT('%',#{keyword},'%' ) or blog_category_name like CONCAT('%',#{keyword},'%' ))
    </if>
    <if test="blogStatus!=null">
        AND blog_status = #{blogStatus}
    </if>
    <if test="blogCategoryId!=null">
        AND blog_category_id = #{blogCategoryId}
    </if>
    order by blog_id desc
    <if test="start!=null and limit!=null">
        limit #{start},#{limit}
    </if>
</select>
```

#### 按传参要求的排序方法（发表日期排序或浏览量排序）返回Blog信息

```java
/**
 * 根据传入的type值返回按发布时间或浏览量排序的BlogList
 * 其中返回的List仅包含id、Title
 * @param type
 * @return
 */
@Override
public List<SimpleBlogListVO> getBlogListForIndexPage(int type) {
    List<SimpleBlogListVO> simpleBlogListVOS = new ArrayList<>();
    //找到俺要求排序的前九个结果
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
```

其中BlogDao对应的getBlogListForIndexPage方法的定义为：

```sql
<select id="findBlogListByType" resultMap="BaseResultMap">
    select *
    from blog
    where is_deleted=0 AND blog_status = 1<!-- 发布状态的文章 -->
    <if test="type!=null and type==0">
        order by blog_views desc
    </if>
    <if test="type!=null and type==1">
        order by blog_id desc
    </if>
    limit #{limit}
</select>
```

其中返回的SimpleBlogListVO的类定义仅包含id、title两变量：

```java
public class SimpleBlogListVO implements Serializable {

    private Long blogId;

    private String blogTitle;

    public Long getBlogId() {
        return blogId;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public String getBlogTitle() {
        return blogTitle;
    }

    public void setBlogTitle(String blogTitle) {
        this.blogTitle = blogTitle;
    }
}
```

前端对应代码：

```html
<th:block th:if="${null != hotBlogs}">
    <th:block th:each="hotBlog : ${hotBlogs}">
        <li class="list-group-item"><a th:href="@{'/blog/' + ${hotBlog.blogId}}">
            <th:block th:text="${hotBlog.blogTitle}"></th:block>
        </a></li>
    </th:block>
</th:block>
```

#### 查询使用按次数排序的Tags

```java
/**
 * 返回Tags及其对应的使用次数
 * @return
 */
@Override
public List<BlogTagCount> getBlogTagCountForIndex() {
    return blogTagDao.getTagCount();
}
```

为此功能创建的BlogTagCount类包含tagId、tagName、tagCount字段

BlogTagDao中getTagCount方法对应的xml配置

```sql
<select id="getTagCount" resultMap="BaseCountResultMap">
    SELECT a.* FROM (
        SELECT t_r.*,t.tag_name FROM
        (SELECT r.tag_id,r.tag_count FROM
         (SELECT tag_id ,COUNT(*) AS tag_count FROM
          (SELECT tr.tag_id FROM blog_tag_relation tr LEFT JOIN blog b ON tr.blog_id = b.blog_id WHERE b.is_deleted=0)
            trb GROUP BY tag_id) r LIMIT 20 ) AS t_r LEFT JOIN tag t ON t_r.tag_id = t.tag_id WHERE t.is_deleted=0) a
    ORDER BY a.tag_count DESC
</select>
```

前端对应代码：

```html
<th:block th:if="${null != hotTags}">
    <th:block th:each="hotTag : ${hotTags}">
        <li class="list-group-item"><a th:href="@{'/tag/' + ${hotTag.tagName}}">
            <th:block th:text="${hotTag.tagName}"></th:block>
            <span class="badge" th:text="${hotTag.tagCount}">1</span>
        </a></li>
    </th:block>
</th:block>
```

### 分类与标签页面的逻辑流程

页面预览：

<img src="https://s1.ax1x.com/2022/04/19/L0lZuR.png" style="zoom:33%;" />

```java
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
```

### 标签列表页逻辑流程

在其他页面（如主页面引入标签信息时会将标签名赋予一跳转链接）：

```html
<th:block th:if="${null != hotTags}">
    <th:block th:each="hotTag : ${hotTags}">
        <li class="list-group-item"><a th:href="@{'/tag/' + ${hotTag.tagName}}">
            <th:block th:text="${hotTag.tagName}"></th:block>
            <span class="badge" th:text="${hotTag.tagCount}">1</span>
        </a></li>
    </th:block>
</th:block>
```

链接指向'/tag/' + ${hotTag.tagName}}"的URL路径

```java
/**
 * 标签列表页
 * @return
 */
@GetMapping({"/tag/{tagName}"})
public String tag(HttpServletRequest request, @PathVariable("tagName") String tagName) {
    return tag(request, tagName, 1);
}

/**
 * 标签列表页
 * @return
 */
@GetMapping({"/tag/{tagName}/{page}"})
public String tag(HttpServletRequest request, @PathVariable("tagName") String tagName, @PathVariable("page") Integer page) {
    PageResult blogPageResult = blogService.getBlogsPageByTag(tagName, page);
    request.setAttribute("blogPageResult", blogPageResult);
    request.setAttribute("pageName", "标签");
    request.setAttribute("pageUrl", "tag");
    request.setAttribute("keyword", tagName);
    request.setAttribute("newBlogs", blogService.getBlogListForIndexPage(1));
    request.setAttribute("hotBlogs", blogService.getBlogListForIndexPage(0));
    request.setAttribute("hotTags", tagService.getBlogTagCountForIndex());
    request.setAttribute("configurations", configService.getAllConfigs());
    return "blog/" + theme + "/list";
}
```

而对具体category的显示逻辑与Tags相同，就不展开赘述了

### 搜索功能的处理

```java
/**
 * 搜索列表页
 *接收keyword参数并将其用search方法处理
 * @return
 */
@GetMapping({"/search"})
public String search(HttpServletRequest request, @RequestParam(value = "keyword",required = false ) String keyword) {
    return search(request, keyword, 1);
}
```

前台搜索表单

```html
<form style="text-align: center" action="http://hjwzqy.online:15498/search/" method="get">
    <div>
        &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
        <label for="search">你想搜索什么?</label>
        <input class="inputText" type="text" id="search" style="color: black" name="keyword" placeholder="请输入欲搜索的内容">
        <button style="color: black">立即搜索</button>
    </div>
</form>
```

search方法处理请求

```java
@GetMapping({"/search/{keyword}/{page}"})
public String search(HttpServletRequest request, @PathVariable("keyword") String keyword, @PathVariable("page") Integer page) {
    PageResult blogPageResult = blogService.getBlogsPageBySearch(keyword, page);
    request.setAttribute("blogPageResult", blogPageResult);
    request.setAttribute("pageName", "搜索");
    request.setAttribute("pageUrl", "search");
    request.setAttribute("keyword", keyword);
    request.setAttribute("newBlogs", blogService.getBlogListForIndexPage(1));
    request.setAttribute("hotBlogs", blogService.getBlogListForIndexPage(0));
    request.setAttribute("hotTags", tagService.getBlogTagCountForIndex());
    request.setAttribute("configurations", configService.getAllConfigs());
    return "blog/" + theme + "/list";
}
```

### 文章详情页

对详情页请求的响应：

```java
/**
 * 详情页
 * @return
 */
@GetMapping({"/blog/{blogId}", "/article/{blogId}"})
public String detail(HttpServletRequest request, @PathVariable("blogId") Long blogId, @RequestParam(value = "commentPage", required = false, defaultValue = "1") Integer commentPage) {
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
```

其中获取文章详情的方法：

```java
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
```

```java
/**
     * 方法抽取
     *
     * @param blog
     * @return
     */
    private BlogDetailVO getBlogDetailVO(Blog blog) {
        if (blog != null && blog.getBlogStatus() == 1) {
            //增加浏览量
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
```

### 评论功能

```java
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
```

 ## 为SpringBoot项目网站配置SSL安全证书，HTTPS

### SSL证书文件的申请与下载

SSL证书的必要条件是有申请的域名，在腾讯云相关服务中申请域名后即可申请SSL证书，签发后的模块如下

![](https://s1.ax1x.com/2022/04/28/LXdM4I.png)

选择jks格式的证书下载

![](https://s1.ax1x.com/2022/04/28/LXdUEj.png)

解压后将文件中的.jks文件复制至SpringBoot项目下的Resource目录下，

![](https://s1.ax1x.com/2022/04/28/LXdTKO.png)

在.properties文件中添加如下配置：

```properties
#使用HTTPS默认的443端口，这样可以达到在网页URL栏端口号的省略显示
server.port=443
#.jks文件名
server.ssl.key-store=classpath:****.jks
#SSL证书的密码
server.ssl.key-store-password=证书密码
server.ssl.keyStoreType=JKS
```

经过如上的配置可以达到在使用HTTPS协议的SSL安全访问，如：https://www.****.com

但若使用http://URL头对网站进行访问会报

```
Bad Request
This combination of host and port requires TLS.
```

错误，原因是没有配置针对http访问的跳转，因此接下来实现HttpToHttpsConfig类来实现对http默认监听端口（80）的https跳转，达到对http报头URL的安全访问

```java
/**
 * 实现http至https的跳转
 */
@Configuration
public class HttpToHttpsConfig {
    /**
     * http重定向到https
     * @return
     */
    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint constraint = new SecurityConstraint();
                constraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                constraint.addCollection(collection);
                context.addConstraint(constraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(httpConnector());
        return tomcat;
    }

    @Bean
    public Connector httpConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        //Connector监听的http的端口号
        connector.setPort(80);
        connector.setSecure(false);
        //监听到http的端口号后转向到的https的端口号
        connector.setRedirectPort(443);
        return connector;
    }

}
```

至此实现了对SpringBoot项目的SSL加密，可以看到网站已可以通过https正常访问

![](https://s1.ax1x.com/2022/04/28/LX0rBF.png)

## 引入Redis优化博客首页排行榜功能的实现

目前博客首页的排行榜功能由MySqL的直接查询实现，虽然也可以达到所要求的目的，但每次对网页的访问都要加载一次，即会调用一次MySQL的查询、排序等功能，在访问量增加后将会使服务端开销、负担变大。

这时可以引用Redis的Zset数据结构来解决这个问题，首先Zset中存储唯一的字串，且每个对象有对应的值，可以对其进行增减，并且可以根据其对应的值返回排序，这很好地贴合了网页排行榜的要求。

### SpringBoot引入Redis

Maven中引入如下依赖：

```xml
<!-- redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- spring2.X集成redis所需common-pool2-->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
    <version>2.6.0</version>
</dependency>
```

配置文件中配置Redis参数

```properties
#Redis配置
#Redis服务器地址
spring.redis.host=192.168.59.129
#Redis服务器连接端口
spring.redis.port=6379
# Redis服务器连接密码
spring.redis.password=PASSWORD
#Redis数据库索引（默认为0）
spring.redis.database= 0
#连接超时时间（毫秒）
spring.redis.timeout=1800000
#连接池最大连接数（使用负值表示没有限制）
spring.redis.lettuce.pool.max-active=-1
#最大阻塞等待时间(负数表示没限制)
spring.redis.lettuce.pool.max-wait=-1
#连接池中的最大空闲连接
spring.redis.lettuce.pool.max-idle=-1
#连接池中的最小空闲连接
spring.redis.lettuce.pool.min-idle=-1
```

创建Redis配置类

```java
@EnableCaching
@Configuration
public class RedisConfig extends CachingConfigurerSupport {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        template.setConnectionFactory(factory);
        //key序列化方式
        template.setKeySerializer(redisSerializer);
        //value序列化
        template.setValueSerializer(jackson2JsonRedisSerializer);
        //value hashmap序列化
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.setHashKeySerializer(template.getKeySerializer());

        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        //解决查询缓存转换异常的问题
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        // 配置序列化（解决乱码的问题）,过期时间600秒
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(600)) //缓存过期10分钟 ---- 业务需求。
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))//设置key的序列化方式
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer)) //设置value的序列化
                .disableCachingNullValues();
        RedisCacheManager cacheManager = RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
        return cacheManager;
    }
}
```

配置完成后对String类型的操作已没有问题，但如要使用Zset数据结构仍需在每次使用前进行`redisTemplate.setValueSerializer(new StringRedisSerializer());`操作，否则会报序列化错误，实例代码如下：

```java
@Autowired
private RedisTemplate redisTemplate;//引入配置

@Test
    public void TestRedis(){
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.opsForZSet().incrementScore("ZZZ","hjw",100);
        Set<Object> zzz = redisTemplate.opsForZSet().reverseRange("ZZZ",0,-1);
//        System.out.println(zzz);
        for(Object s : zzz){
            System.out.println(s.toString());
        }
//        redisTemplate.opsForValue().set("name","hjw");
//        String name = (String)redisTemplate.opsForValue().get("name");
//        System.out.println(name);
    }
```

### 配置Redis初始化类

实现ApplicationRunner接口的类，重写其run方法后即可做到在项目开始时执行所需代码的效果，这里要在项目启动时确保对Redis进行过初始化操作：

```java
/**
 * 项目初始化操作（在SpringBoot项目运行时执行）
 * 为了在第一次启用Redis时写入Zset
 */
@Component
public class RedisInit implements ApplicationRunner {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private BlogDao blogDao;

    @Autowired
    private BlogTagDao blogTagDao;

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        序列化
        redisTemplate.setValueSerializer(new StringRedisSerializer());
//        检查项目是否是第一次使用Redis
        Set<Object> viewCount = redisTemplate.opsForZSet().reverseRange("ViewCount", 0, 9);
        Set<Object> tagCount = redisTemplate.opsForZSet().reverseRange("TagCount", 0, 19);
//        如果是则执行初始化操作，不是不执行任何操作
        if(viewCount.size() == 0 || tagCount.size() == 0){
//            获取所有Blog
            List<Blog> blogList = blogDao.findBlogList(null);
            for(Blog blog : blogList){
//                将ViewCount的Zset传入SQL查到的存储的访问量，Key以ID+Title的格式存储
                redisTemplate.opsForZSet().add("ViewCount",blog.getBlogId().toString() + ","+ blog.getBlogTitle(),blog.getBlogViews());
                String[] tags = blog.getBlogTags().split(",");
                for(String tag : tags){
//                    获取存储此Tag的类
                    BlogTag blogTag = blogTagDao.selectByTagName(tag);
//                    此Tag的使用次数++
                    redisTemplate.opsForZSet().incrementScore("TagCount",blogTag.getTagId().toString()+","+tag,1);
                }
            }
        }
        //        是否使用过Redis来存储最新博客，如果未使用过则初始化对应Redis
        if(redisTemplate.opsForList().size("NewBlog") == 0){
            List<Blog> blogList = blogDao.findBlogList(null);
            for(Blog blog : blogList){
                redisTemplate.opsForList().rightPush("NewBlog",blog.getBlogId().toString() + ","+ blog.getBlogTitle());
            }
        }
    }
}
```

### 获取排行榜信息部分代码

每次请求访问有排行榜的页面时，后台会从Redis从取出对应的信息并返回前端显示：

```java
request.setAttribute("newBlogs", blogService.getBlogListForNewBlog());//查询按发布时间排序的文章
request.setAttribute("hotBlogs", blogService.getBlogListForViewCount());//查询按点击量排序的文章
request.setAttribute("hotTags", tagService.getBlogTagCount());//查询按使用次数排序的Tag
```

可以看出主要有三种以不同信息分开的排行榜

#### 按发布时间排序的文章

NewBlog的预处理为在每次新增博客的时候将博客信息（ID、Title）LeftAdd加入List中。（对博客的修改不进行处理）

```java
@Override
    public List<SimpleBlogListVO> getBlogListForNewBlog() {
        List<SimpleBlogListVO> simpleBlogListVOS = new ArrayList<>();
        redisTemplate.setValueSerializer(new StringRedisSerializer());
//        返回最近插入的9个博客信息的集合
        List newBlog = redisTemplate.opsForList().range("NewBlog", 0, 8);
        for(Object b : newBlog){
            String blog = b.toString();
//            将信息以','分隔，即分隔了BlogID及Tital
            String[] blogg = blog.split(",");
            SimpleBlogListVO simpleBlogListVO = new SimpleBlogListVO();
            simpleBlogListVO.setBlogId(Long.valueOf(blogg[0]));
            simpleBlogListVO.setBlogTitle(blogg[1]);
            simpleBlogListVOS.add(simpleBlogListVO);
        }
        return simpleBlogListVOS;
    }
```

#### 按点击量排序的文章

ViewCount的预处理为每次有对Blog文章的访问时对应在Zset中存储的项值++

```java
@Override
    public List<SimpleBlogListVO> getBlogListForViewCount() {
        List<SimpleBlogListVO> simpleBlogListVOS = new ArrayList<>();
//        按点击次数从高到低取前九名
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
```

#### 按使用次数排序的Tag

获取Tag信息由于要同时返回Tag的使用次数，因此相比前两个要略微复杂一些

对TagCount的预处理为：

1. 新增文章时将其对应的Tag信息++；
2. 修改文章时先将修改前的Tag对应的项--，再将修改后的Tag项++。

```java
@Override
    public List<BlogTagCount> getBlogTagCount() {
        List<BlogTagCount> res = new ArrayList<>();
        redisTemplate.setValueSerializer(new StringRedisSerializer());
//        返回Tag及其对应在Zset中的值
        Set<ZSetOperations.TypedTuple<Object>> tagCount = redisTemplate.opsForZSet().reverseRangeWithScores("TagCount", 0, 19);
//        用iterator读取set中的值
        Iterator<ZSetOperations.TypedTuple<Object>> iterator = tagCount.iterator();
        while (iterator.hasNext()){
            ZSetOperations.TypedTuple<Object> typedTuple = iterator.next();
//            获取Tag名及ID信息
            String tag = typedTuple.getValue().toString();
            String[] t = tag.split(",");
//            获取其对应的使用次数
            float f = Float.valueOf(typedTuple.getScore().toString());
            int count = (int)f;
            BlogTagCount blogTagCount = new BlogTagCount();
            blogTagCount.setTagId(Integer.valueOf(t[0]));
            blogTagCount.setTagName(t[1]);
            blogTagCount.setTagCount(count);
            res.add(blogTagCount);
        }
        return res;
    }
```



### 引入Redis处理排行榜功能前后网站的并发性能对比

#### 用MySQL实现时网页的并发性能

用JMeter模拟对网页的Http访问请求

1s100个请求，平均时间2661

![](https://s1.ax1x.com/2022/05/16/OWfIIg.png)

![](https://s1.ax1x.com/2022/05/16/OWfbzn.png)

1s250个请求，平均9802

![](https://s1.ax1x.com/2022/05/16/OWft2R.png)

![](https://s1.ax1x.com/2022/05/16/OWfNx1.png)

1s500个请求，平均时间25120

![](https://s1.ax1x.com/2022/05/16/OWfAUg.png)

![](https://s1.ax1x.com/2022/05/16/OWfE5Q.png)



一秒钟之内发送1000个请求，结果如下：

![](https://s1.ax1x.com/2022/05/15/ORMWLQ.png)

可以看到Sample Time（每个请求所花时间，单位毫秒）的平均值为33937ms，随着并发次数的增加而呈现快速升高的趋势，且在并发的后部分会出现大量访问请求失败的情况，说明并发性能还存在优化的空间（当然也和服务器的性能较差有关）。

![](https://s1.ax1x.com/2022/05/15/ORQpJx.png)

#### 使用Redis缓存实现排名后网页的并发性能

1s内500个访问请求，结果如下，平均16076-20682：

![](https://s1.ax1x.com/2022/05/16/OWgPkd.png)

![](https://s1.ax1x.com/2022/05/16/OWgu7Q.png)

可以看到通过引入Redis，切实提高了网页的并发性（36%左右）。



## 使用JavaMailSender配合MySQL数据库实现对网站的订阅功能

### 引入依赖

```xml
<!--邮件发送-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
```

### 添加yml配置

```properties
spring:
  mail:
    host: smtp.163.com
    password: //邮箱授权码
    username: //邮箱地址
    port:
    default-encoding: UTF-8
    protocol: smtp
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
      mail.smtp.starttls.required: true
      mail.smtp.socketFactory.port: 465
      mail.smtp.socketFactory.class: javax.net.ssl.SSLSocketFactory
      mail.smtp.socketFactory.fallback: false
```

### Service及DAO层的代码编写

实现MailService接口

```java
public interface MailService {
    /**
     * 邮件发送
     * @param to
     * @param subject
     * @param text
     * @return
     */
    boolean sendMail(String to, String subject, String text);
}
```

实现类

```java
@Service
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public boolean sendMail(String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        try {
            mailSender.send(msg);
        } catch (MailException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
        return true;
    }
}
```

DAO层

```java
public interface SubEmailsDao {

    int insertEmail(String address);

    List<SubEmails> getAllEmails();

    SubEmails getEmailByAddress(String address);
}
```

### 订阅功能的实现

```java
@GetMapping({"/sub"})
    public String sub(HttpServletRequest request, @RequestParam(value = "address",required = false ) String address){
        String Title = "订阅成功确认邮件";
        String context = "恭喜！！\r\n如果你收到此邮件说明你已经成功订阅本网站，感谢您对本网站的支持！！！此后将在每次更新文章时自动向您留下的邮箱发送信息，请注意查收\n" +
                " 此邮件为自动发送，请勿回复。\r\n-------HJW";
        mailService.sendMail(address,Title,context); //发送订阅确认信息
        SubEmails byAddress = subEmailsDao.getEmailByAddress(address); //查找是否订阅过
        if(byAddress == null) subEmailsDao.insertEmail(address); //若之前没有订阅过则新添加邮箱至订阅表中
        return detail(request,(long)81,1);
    }
```

### 新文章的邮件推送信息

```java
Long id = blogDao.getIdByBlogTitle(blogTitle);
        if(res.equals("success")){
            List<SubEmails> emails = subEmailsDao.getAllEmails();
            String subject = "HJWsBlog有题为《" + blogTitle + "》的文章发布啦！";
            String begin = "您好！感谢您对本网站的订阅！\r\n本人最新发表了一篇题为《" + blogTitle + "》的文章，以下为文章链接，欢迎点击查看，希望可以对您有所帮助！";
            String url = "https://hjwzqy.online/blog/" + id.toString();
            String end = "如果您想取消对本网站的订阅，请添加QQ937529137联系管理员取消！\r\n 此邮件为自动发送，请勿回复。\r\n" +
                    "-------HJW";
            for(SubEmails email : emails){
                mailService.sendMail(email.getAddress(),subject,begin + "\r\n" + url+"\r\n"+end);
            }
            return ResultGenerator.genSuccessResult("添加成功");
        }else{
            return ResultGenerator.genFailResult(res);
        }
    }
```

由此将在每次更新文章时对订阅表中的所有邮箱地址进行推送。实现了网页的邮箱订阅功能。

## 引入RabbitMQ实现在发送邮件时的异步处理

### 导入依赖

```xml
  <!-- pom.xml rabbitmq -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
```

### 配置yml文件

注意缩进

```yml
 #RabbitMQ配置
  rabbitmq:
    host:
    port: 5672
    username: username
    password: password
    virtual-host: /
    connection-timeout: 10000
    listener:
      simple:
        acknowledge-mode: auto # 自动应答
        auto-startup: true
        default-requeue-rejected: false # 不重回队列
        concurrency: 5
        max-concurrency: 20
        prefetch: 1 # 每次只处理一个信息
        retry:
          enabled: false
    template:
      exchange: web.demo
      routing-key: user.key
```

### 配置消息传递实例

```java
public class messageToSend implements Serializable {
    private String address;
    private String Subject;
    private String MainText;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public String getMainText() {
        return MainText;
    }

    public void setMainText(String mainText) {
        MainText = mainText;
    }
}
```



### 创建消费者监听消息队列，如有信息则马上对信息进行邮件发送处理

```java
@Component
public class MailConsumer {

    @Autowired
    MailService mailService;

    @RabbitListener(bindings =
    @QueueBinding(
            value = @Queue, //临时队列
            exchange = @Exchange(name = "mailExchange",type = "topic"), //指定交换机名称与类型
            key = {"user.*"}
    )
    )
    private void sendEMail(messageToSend message){
        mailService.sendMail(message.getAddress(), message.getSubject(), message.getMainText());
    }
}
```

### 消息的发送部分

负责将邮箱、当前文章标题等信息发送至队列中，供后续异步进行处理

```java
@Autowired
    RabbitTemplate rabbitTemplate;


List<SubEmails> emails = subEmailsDao.getAllEmails();
            String subject = "HJWsBlog有题为《" + blogTitle + "》的文章发布啦！";
            String begin = "您好！感谢您对本网站的订阅！\r\n本人最新发表了一篇题为《" + blogTitle + "》的文章，以下为文章链接，欢迎点击查看，希望可以对您有所帮助！";
            String url = "https://hjwzqy.online/blog/" + id.toString();
            String end = "如果您想取消对本网站的订阅，请添加QQ937529137联系管理员取消！\r\n 此邮件为自动发送，请勿回复。\r\n" + "-------HJW";
            for(SubEmails email : emails){
                messageToSend message = new messageToSend();
                message.setAddress(email.getAddress());
                message.setSubject(subject);
                message.setMainText(begin + "\r\n" + url+"\r\n"+end);
                rabbitTemplate.convertAndSend("mailExchange","user.mail",message);
```

消息已经发送即可在网页的15672端口视图上得到对应信息

![](https://s1.ax1x.com/2022/07/25/jvjvBq.png)

通过数据可知使用MQ进行异步处理后会使前台响应时间由42s降至2s，用户体验提升巨大。

## 为项目添加全局异常处理

当前项目并未对项目的异常情况进行处理，如用户进行一些非法请求或访问将会进入默认错误界面，非常简陋难看，影响用户体验。

如下图

![](https://s1.ax1x.com/2022/07/30/viXAeg.png)

针对这种情况可以通过引入全局异常处理来解决这个问题，捕获异常跳转到指定页面。

### 引入Controller来捕获异常

```java
@Controller
public class ErrorPageController implements ErrorController {
    private static ErrorPageController errorPageController;

    @Autowired
    private ErrorAttributes errorAttributes;

    private final static String ERROR_PATH = "/error";

    public ErrorPageController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    public ErrorPageController() {
        if (errorPageController == null) {
            errorPageController = new ErrorPageController(errorAttributes);
        }
    }

    @RequestMapping(value = ERROR_PATH, produces = "text/html")
    public ModelAndView errorHtml(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        if (HttpStatus.BAD_REQUEST == status) {
            return new ModelAndView("error/error_400");
        } else if (HttpStatus.NOT_FOUND == status) {
            return new ModelAndView("error/error_404");
        } else {
            return new ModelAndView("error/error_5xx");
        }
    }

    @RequestMapping(value = ERROR_PATH)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> body = getErrorAttributes(request, getTraceParameter(request));
        HttpStatus status = getStatus(request);
        return new ResponseEntity<Map<String, Object>>(body, status);
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }


    private boolean getTraceParameter(HttpServletRequest request) {
        String parameter = request.getParameter("trace");
        if (parameter == null) {
            return false;
        }
        return !"false".equals(parameter.toLowerCase());
    }

    protected Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        WebRequest webRequest = new ServletWebRequest(request);
        return this.errorAttributes.getErrorAttributes(webRequest, includeStackTrace);
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request
                .getAttribute("javax.servlet.error.status_code");
        if (statusCode != null) {
            try {
                return HttpStatus.valueOf(statusCode);
            } catch (Exception ex) {
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
```

### 异常界面

以400错误状态码为例，简单引用CSS构建异常页面，HTML代码如下：

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8" /><meta http-equiv="X-UA-Compatible" content="IE=edge" /><meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>We've got some trouble | 500 - Webservice currently unavailable</title>
    <style type="text/css">/*! //省略源地址</style>
</head>
<body>
<div class="cover">
    <h1>Webservice currently unavailable <small>Error 500</small></h1>
    <p class="lead">An unexpected condition was encountered.<br />Our service team has been dispatched to bring it back online.</p>
    <p>回到首页 <a href="https://hjwzqy.online" target="_blank">HJWs Blog</a></p>
</div>
</body>
</html>

```

上线实际效果如下：

![](https://s1.ax1x.com/2022/07/30/viO7z6.png)

点击下方链接体验^_^

[博客报错页](https://hjwzqy.online/hjw)

## 对Full GC问题的排查与解决

### 问题产生

最近在使用博客后台的文章列表功能时（视图如下），发现在不同页进行切换时卡顿现象很明显，使用体验不佳，因此对其进行排查

![](https://s1.ax1x.com/2022/08/26/v2yjB9.png)

会在较长时间处于如下状态

![](https://s1.ax1x.com/2022/08/26/v26P1O.png)

### 问题排查定位

出现这种问题第一时间想到的是是否是服务器CPU性能不佳导致，因此先用`top -H -p 12495`指令对CPU使用率进行排查，结果如下：

![](https://s1.ax1x.com/2022/08/26/v2yZWT.png)

可以看到登录线程（即第一个）对CPU的使用率不高，仅为16.3%，所以初步排除为服务器性能问题，接下来对GC进行排查

使用`jstat -gcutil 12495 5s`指令对GC历史进行排查，结果如下：

![](https://s1.ax1x.com/2022/08/26/v2yEF0.png)

可以看到明显存在问题，在项目部署时间不长的情况下（YGC都仅有130+次），FGC竟然进行过200+次，众所周知，每次进行FGC都会STW，这显然会导致使用缓慢卡顿，因此对FGC进行排查，引起FGC的主要原因显然是堆存储所占比率过大导致超过阈值，此时JVM会自动进行FGC来回收垃圾释放空间。

基于这个思路进行分析，但由上图发现JVM堆中老年代使用占比仅为59.84%，十分健康良好，远达不到阈值，遂判断并不是堆空间过大导致（如内存泄漏等问题）的FGC。

那么还有可能存在的问题就是代码中手动调用了System.gc()导致JVM进行FGC，进过分析发现这种情况也显然符合本次情况。

### 问题产生原因

经过对代码的排查可定位至如下代码：

```java
public PageResult getBlogsPage(PageQueryUtil pageUtil) {
    List<Blog> blogList = blogDao.findBlogList(pageUtil);//根据页数获取Blogs
    int totalBlogs = blogDao.getTotalBlogs(null);
    PageResult pageResult = new PageResult(blogList, totalBlogs, pageUtil.getLimit(), pageUtil.getPage());
    System.gc();//罪魁祸首在此
    return pageResult;
}
```

这段代码是在用户查找文章列表信息时调用的，会根据用户设置的pageUtil来返回相应的Blog，SQL如下：

```xml
<select id="findBlogList" resultMap="BaseResultMap" parameterType="Map">
    select * from blog
    where is_deleted=0
    <if test="keyword!=null">
        AND (blog_title like CONCAT('%',#{keyword},'%' ) or blog_category_name like CONCAT('%',#{keyword},'%' ))
    </if>
    <if test="blogStatus!=null">
        AND blog_status = #{blogStatus}
    </if>
    <if test="blogCategoryId!=null">
        AND blog_category_id = #{blogCategoryId}
    </if>
    order by blog_id desc
    <if test="start!=null and limit!=null">
        limit #{start},#{limit}
    </if>
</select>
```

而由于每次查找Blog都会查找所有元素（包括很占内存的文章主体信息），当时也没有对其进行合理的优化，只是在每次查找后暴力的通过调用System.gc()来清理垃圾，这也就导致了频繁进行调用时产生的大量FGC，最终导致用户体验下降。

### 解决方案

经过这次问题，首先显然不能再在代码中调用System.gc()来解决了，因此随后采用了一种个人称之为查询分离的策略，即在不需要查询所有内容时仅返回需要的Blog对象，之后XML中返回值的定义如下：

```xml
<resultMap id="BaseResultMap" type="com.hjwsblog.hjwsblog.entity.Blog">
    <id column="blog_id" jdbcType="BIGINT" property="blogId"/>
    <result column="blog_title" jdbcType="VARCHAR" property="blogTitle"/>
    <result column="blog_sub_url" jdbcType="VARCHAR" property="blogSubUrl"/>
    <result column="blog_cover_image" jdbcType="VARCHAR" property="blogCoverImage"/>
    <result column="blog_category_id" jdbcType="INTEGER" property="blogCategoryId"/>
    <result column="blog_category_name" jdbcType="VARCHAR" property="blogCategoryName"/>
    <result column="blog_tags" jdbcType="VARCHAR" property="blogTags"/>
    <result column="blog_status" jdbcType="TINYINT" property="blogStatus"/>
    <result column="blog_views" jdbcType="BIGINT" property="blogViews"/>
    <result column="enable_comment" jdbcType="TINYINT" property="enableComment"/>
    <result column="is_deleted" jdbcType="TINYINT" property="isDeleted"/>
    <result column="create_time" jdbcType="TIMESTAMP" property="createTime"/>
    <result column="update_time" jdbcType="TIMESTAMP" property="updateTime"/>
</resultMap>

<resultMap extends="BaseResultMap" id="ResultMapWithBLOBs" type="com.hjwsblog.hjwsblog.entity.Blog">
    <result column="blog_content" jdbcType="LONGVARCHAR" property="blogContent"/>
</resultMap>
```

如此将返回对象的大小大大缩减，也就无需进行GC

```java
public PageResult getBlogsPage(PageQueryUtil pageUtil) {
    List<Blog> blogList = blogDao.findBlogList(pageUtil);
    int totalBlogs = blogDao.getTotalBlogs(null);
    PageResult pageResult = new PageResult(blogList, totalBlogs, pageUtil.getLimit(), pageUtil.getPage());
    return pageResult;
}
```
