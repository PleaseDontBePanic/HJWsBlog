package com.hjwsblog.hjwsblog.controller.admin;

import com.hjwsblog.hjwsblog.service.TagService;
import com.hjwsblog.hjwsblog.util.PageQueryUtil;
import com.hjwsblog.hjwsblog.util.Result;
import com.hjwsblog.hjwsblog.util.ResultGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class TagController {
    @Resource
    private TagService tagService;

    @GetMapping("/tags")
    public String tag(HttpServletRequest request){
        request.setAttribute("path","tags");
        return "admin/tag";
    }

    @GetMapping("/tags/list")
    @ResponseBody
    public Result list(@RequestParam Map<String, Object> params){
        if (StringUtils.isEmpty(params.get("page")) || StringUtils.isEmpty(params.get("limit"))) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(tagService.getBlogTagPage(pageUtil));
    }

    @PostMapping("/tags/save")
    @ResponseBody
    public Result save(@RequestParam("tagName") String tagName){
        if (StringUtils.isEmpty(tagName)) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        if(tagService.saveTag(tagName)){
            return ResultGenerator.genSuccessResult();
        }else {
            return ResultGenerator.genFailResult("标签名称重复");
        }
    }

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
}
