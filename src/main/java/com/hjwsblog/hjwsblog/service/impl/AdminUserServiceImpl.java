package com.hjwsblog.hjwsblog.service.impl;

import com.hjwsblog.hjwsblog.Dao.AdminUserDao;
import com.hjwsblog.hjwsblog.entity.AdminUser;
import com.hjwsblog.hjwsblog.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private AdminUserDao adminUserDao;

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

    @Override
    public boolean updateName(Integer loginUserId, String loginUserName) {
        AdminUser user = new AdminUser();
        user.setLogin_user_id(loginUserId);
        user.setLogin_user_name(loginUserName);
        return adminUserDao.updateUserName(user) > 0;
    }

    @Override
    public boolean updatePassword(Integer loginUserId, String originalPassword, String newPassword) {
        String password = adminUserDao.getPasswordById(loginUserId);
        if(!password.equals(originalPassword)) return false;
        AdminUser user = new AdminUser();
        user.setLogin_user_id(loginUserId);
        user.setLogin_user_password(newPassword);
        return adminUserDao.updateUserPassword(user) > 0;
    }
}
