package com.hjwsblog.hjwsblog.Dao;

import com.hjwsblog.hjwsblog.entity.AdminUser;

import java.util.List;

public interface AdminUserDao {

    List<AdminUser> findAllUsers();

    int updateUserName(AdminUser user);

    String getPasswordById(Integer id);

    int updateUserPassword(AdminUser user);
}
