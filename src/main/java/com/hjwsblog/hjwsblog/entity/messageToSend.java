package com.hjwsblog.hjwsblog.entity;

import javax.security.auth.Subject;
import java.io.Serializable;

public class messageToSend implements Serializable {
    private String address;
    private String Subject;
    private String MainText;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public String getMainText() {
        return MainText;
    }

    public void setMainText(String mainText) {
        MainText = mainText;
    }
}
