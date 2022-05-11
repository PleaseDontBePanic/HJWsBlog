package com.hjwsblog.hjwsblog.controller.TestControllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

//标注此类为Controller类
@Controller
public class HelloController {

    @GetMapping("/attributes")
    public String attributes(HttpServletRequest request ){
        request.setAttribute("title","使用Thymeleaf标签演示");
        request.setAttribute("th_id","Thy_input");
        request.setAttribute("th_name","Thy_name");
        request.setAttribute("th_value",8);
        request.setAttribute("th_class","Thy_class");
        request.setAttribute("th_href","https://hjwzqy.online");
        return "attributes";
    }

    @GetMapping("/thymeleaf")
    public String hello(HttpServletRequest request ,@RequestParam(value = "description",required = false ) String description){
        request.setAttribute("description","传值为:"+description);
        return "thymeleaf";
    }

    @GetMapping("/info")
    @ResponseBody
    public String info(){
        return "This is a demo!";
}

}
