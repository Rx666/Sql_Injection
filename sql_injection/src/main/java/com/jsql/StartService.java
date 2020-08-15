package com.jsql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.jsql.mapper.UrlMapper;
import com.jsql.util.SpringContextUtils;


/**
 * 继承 ApplicationRunner 接口后项目启动时会按照执行顺序执行 run 方法
 * @author 12655
 *
 */
@Component
@Order(value = 1)
public class StartService implements ApplicationRunner {

	@Autowired
	UrlMapper urlMapper;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		urlMapper.deleteUrl();
	}
}
