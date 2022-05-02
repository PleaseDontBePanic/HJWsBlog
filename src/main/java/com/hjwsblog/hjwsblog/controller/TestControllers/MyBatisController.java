package com.hjwsblog.hjwsblog.controller.TestControllers;

import com.hjwsblog.hjwsblog.Dao.UserDao;
import com.hjwsblog.hjwsblog.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class MyBatisController {
    @Resource
    UserDao userDao;

    @GetMapping("/users/mybatis/findAll")
    public List<User> queryAll(){
        return userDao.findAllUsers();
    }

    @GetMapping("users/mybatis/insert")
    public boolean insert(String name , String password){
        if(name.isEmpty() || password.isEmpty()){
            return false;
        }
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        return userDao.insertUser(user) > 0;
    }

    @GetMapping("users/mybatis/update")
    public boolean update(Integer id , String name , String password){
        if(id == null || id < 1 || name.isEmpty() || password.isEmpty()){
            return false;
        }
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setPassword(password);
        return userDao.updateUser(user) > 0;
    }
    @GetMapping("users/mybatis/delete")
    public boolean delete(Integer id){
        if(id == null || id < 1){
            return false;
        }
        return userDao.deleteUser(id) > 0;
    }

}
