package com.hjwsblog.hjwsblog.service;

import com.hjwsblog.hjwsblog.entity.BlogLink;
import com.hjwsblog.hjwsblog.util.PageQueryUtil;
import com.hjwsblog.hjwsblog.util.PageResult;

import java.util.List;
import java.util.Map;

public interface LinkService {
    int getTotalLinks();

    PageResult getBlogLinkPage(PageQueryUtil pageUtil);

    boolean saveLink(BlogLink link);

    BlogLink selectById(Integer id);

    boolean deleteBatch(Integer[] ids);

    boolean updateLink(BlogLink tempLink);

    Map<Byte, List<BlogLink>> getLinksForLinkPage();
}
