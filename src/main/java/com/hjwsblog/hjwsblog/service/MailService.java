package com.hjwsblog.hjwsblog.service;

public interface MailService {
    /**
     * 邮件发送
     * @param to
     * @param subject
     * @param text
     * @return
     */
    boolean sendMail(String to, String subject, String text);
}
