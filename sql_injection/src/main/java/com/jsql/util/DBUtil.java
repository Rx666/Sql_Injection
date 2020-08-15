package com.jsql.util;

import com.jsql.mapper.UrlMapper;


/**
 * 	数据层工具
 * @author 12655
 *
 */
public class DBUtil {
    

	UrlMapper urlMapper = SpringContextUtils.getBean(UrlMapper.class);

    public UrlMapper getUrlMapper() {
        return this.urlMapper;
    }

}
