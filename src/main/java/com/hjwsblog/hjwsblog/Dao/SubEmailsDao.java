package com.hjwsblog.hjwsblog.Dao;

import com.hjwsblog.hjwsblog.entity.SubEmails;

import java.util.List;

public interface SubEmailsDao {

    int insertEmail(String address);

    List<SubEmails> getAllEmails();

    SubEmails getEmailByAddress(String address);
}
