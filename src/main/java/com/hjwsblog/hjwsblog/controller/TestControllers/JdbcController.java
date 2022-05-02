package com.hjwsblog.hjwsblog.controller.TestControllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class JdbcController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/user/queryall")
    public List<Map<String,Object>> queryAll(){
        List<Map<String,Object>> list = jdbcTemplate.queryForList("select * from tb_user");
        return list;
    }

    @GetMapping("/user/insert")
    public Object insert(String name , String password){
        jdbcTemplate.execute("insert into tb_user(`name`,`password`) value (\""+ name + "\" , \"" + password +"\")");
        return true;
    }
}
