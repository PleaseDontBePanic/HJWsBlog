package com.hjwsblog.hjwsblog.controller.admin;

import com.hjwsblog.hjwsblog.service.CategoryService;
import com.hjwsblog.hjwsblog.util.PageQueryUtil;
import com.hjwsblog.hjwsblog.util.Result;
import com.hjwsblog.hjwsblog.util.ResultGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用于处理category栏目中的请求
 */
@Controller
@RequestMapping("/admin")
public class CategoryController {
    @Resource
    private CategoryService categoryService;

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
        return ResultGenerator.genSuccessResult(categoryService.getBlogCategoryPage(pageUtil));
    }

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


}
