package com.hjwsblog.hjwsblog.service;

import com.hjwsblog.hjwsblog.entity.AdminUser;

public interface AdminUserService {
    AdminUser login(String userName , String password);
    boolean updateName(Integer loginUserId, String loginUserName );
    boolean updatePassword(Integer loginUserId,String originalPassword,String newPassword);
}
