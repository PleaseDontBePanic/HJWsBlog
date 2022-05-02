package com.hjwsblog.hjwsblog.controller.TestControllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @RequestMapping("/test/type/conversion")
    public void typeConversion(String goodsname , float weight , int type , boolean onsale){
        System.out.println("goodsname:"+goodsname);
        System.out.println("weight:"+weight);
        System.out.println("type:"+type);
        System.out.println("onsale:"+onsale);
    }
}
