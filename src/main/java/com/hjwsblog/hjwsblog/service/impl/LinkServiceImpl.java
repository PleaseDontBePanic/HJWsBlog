package com.hjwsblog.hjwsblog.service.impl;

import com.hjwsblog.hjwsblog.Dao.BlogLinkDao;
import com.hjwsblog.hjwsblog.entity.BlogLink;
import com.hjwsblog.hjwsblog.service.LinkService;
import com.hjwsblog.hjwsblog.util.PageQueryUtil;
import com.hjwsblog.hjwsblog.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class LinkServiceImpl implements LinkService {

    @Autowired
    private BlogLinkDao blogLinkDao;

    @Override
    public int getTotalLinks() {
        return blogLinkDao.getTotalLinks();
    }

    @Override
    public PageResult getBlogLinkPage(PageQueryUtil pageUtil) {
        List<BlogLink> linkList = blogLinkDao.findLinkList(pageUtil);
        int total = blogLinkDao.getTotalLinks();
        PageResult pageResult = new PageResult(linkList, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public boolean saveLink(BlogLink link) {
        return blogLinkDao.insertSelective(link) > 0;
    }

    @Override
    public BlogLink selectById(Integer id) {
        return blogLinkDao.selectById(id);
    }

    @Override
    public boolean deleteBatch(Integer[] ids) {
        return blogLinkDao.deleteBatch(ids) > 0;
    }

    @Override
    public boolean updateLink(BlogLink tempLink) {
        return blogLinkDao.updateByPrimaryKeySelective(tempLink) > 0;
    }

    @Override
    public Map<Byte, List<BlogLink>> getLinksForLinkPage() {
        //获取所有链接数据
        List<BlogLink> links = blogLinkDao.findLinkList(null);
        if (!CollectionUtils.isEmpty(links)) {
            //根据type进行分组
            Map<Byte, List<BlogLink>> linksMap = links.stream().collect(Collectors.groupingBy(BlogLink::getLinkType));
            return linksMap;
        }
        return null;
    }
}
