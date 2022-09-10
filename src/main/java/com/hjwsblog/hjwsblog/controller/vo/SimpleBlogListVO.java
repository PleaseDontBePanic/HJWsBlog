package com.hjwsblog.hjwsblog.controller.vo;

import java.io.Serializable;

public class SimpleBlogListVO implements Serializable {

    private  Integer blogCount;

    private Long blogId;

    private String blogTitle;

    public Integer getBlogCount() {
        return blogCount;
    }

    public void setBlogCount(Integer blogCount) {
        this.blogCount = blogCount;
    }

    public Long getBlogId() {
        return blogId;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public String getBlogTitle() {
        return blogTitle;
    }

    public void setBlogTitle(String blogTitle) {
        this.blogTitle = blogTitle;
    }
}
