package com.hjwsblog.hjwsblog.service.impl;

import com.hjwsblog.hjwsblog.Dao.BlogConfigDao;
import com.hjwsblog.hjwsblog.entity.BlogConfig;
import com.hjwsblog.hjwsblog.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private BlogConfigDao blogConfigDao;

    public static final String websiteName = "HJWs Blog";
    public static final String websiteDescription = "HJWs Blog 是HJW搭建的个人技术生活博客，用于记录平时的学习及生活";
    public static final String websiteLogo = "/admin/dist/img/logo2.png";
    public static final String websiteIcon = "/admin/dist/img/favicon.ico";

    public static final String yourAvatar = "/admin/dist/img/1favicon.ico";
    public static final String yourEmail = "13633457537@163.com";
    public static final String yourName = "HandsomeHJW";

    public static final String footerAbout = "HJWs Blog";
    public static final String footerICP = "川ICP备";
    public static final String footerCopyRight = "@2022 HandsomeHJW";
    public static final String footerPoweredBy = "HJW";
    public static final String footerPoweredByURL = "##";

    @Override
    public Map<String, String> getAllConfigs() {
        List<BlogConfig> blogConfigs = blogConfigDao.selectAll();
        Map<String, String> configMap = blogConfigs.stream().collect(Collectors.toMap(BlogConfig::getConfigName, BlogConfig::getConfigValue));
        for (Map.Entry<String, String> config : configMap.entrySet()) {
            if ("websiteName".equals(config.getKey()) && StringUtils.isEmpty(config.getValue())) {
                config.setValue(websiteName);
            }
            if ("websiteDescription".equals(config.getKey()) && StringUtils.isEmpty(config.getValue())) {
                config.setValue(websiteDescription);
            }
            if ("websiteLogo".equals(config.getKey()) && StringUtils.isEmpty(config.getValue())) {
                config.setValue(websiteLogo);
            }
            if ("websiteIcon".equals(config.getKey()) && StringUtils.isEmpty(config.getValue())) {
                config.setValue(websiteIcon);
            }
            if ("yourAvatar".equals(config.getKey()) && StringUtils.isEmpty(config.getValue())) {
                config.setValue(yourAvatar);
            }
            if ("yourEmail".equals(config.getKey()) && StringUtils.isEmpty(config.getValue())) {
                config.setValue(yourEmail);
            }
            if ("yourName".equals(config.getKey()) && StringUtils.isEmpty(config.getValue())) {
                config.setValue(yourName);
            }
            if ("footerAbout".equals(config.getKey()) && StringUtils.isEmpty(config.getValue())) {
                config.setValue(footerAbout);
            }
            if ("footerICP".equals(config.getKey()) && StringUtils.isEmpty(config.getValue())) {
                config.setValue(footerICP);
            }
            if ("footerCopyRight".equals(config.getKey()) && StringUtils.isEmpty(config.getValue())) {
                config.setValue(footerCopyRight);
            }
            if ("footerPoweredBy".equals(config.getKey()) && StringUtils.isEmpty(config.getValue())) {
                config.setValue(footerPoweredBy);
            }
            if ("footerPoweredByURL".equals(config.getKey()) && StringUtils.isEmpty(config.getValue())) {
                config.setValue(footerPoweredByURL);
            }
        }
        return configMap;
    }

    @Override
    public int updateConfig(String websiteName, String setValue) {
        BlogConfig blogConfig = blogConfigDao.selectByPrimaryKey(websiteName);
        if(blogConfig != null){
            blogConfig.setConfigValue(setValue);
            blogConfig.setUpdateTime(new Date());
            return blogConfigDao.updateByPrimaryKeySelective(blogConfig);
        }
        return 0;
    }
}
