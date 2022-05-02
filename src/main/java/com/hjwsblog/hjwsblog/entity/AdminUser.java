package com.hjwsblog.hjwsblog.entity;

public class AdminUser {

    private Integer login_user_id;
    private String login_user_name;
    private String login_user_password;

    public Integer getLogin_user_id() {
        return login_user_id;
    }

    public void setLogin_user_id(Integer login_user_id) {
        this.login_user_id = login_user_id;
    }

    public String getLogin_user_name() {
        return login_user_name;
    }

    public void setLogin_user_name(String login_user_name) {
        this.login_user_name = login_user_name;
    }

    public String getLogin_user_password() {
        return login_user_password;
    }

    public void setLogin_user_password(String login_user_password) {
        this.login_user_password = login_user_password;
    }
}
