package com.jsql.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/** 配置文件常量配置 */
@Configuration
public class ConstantConfig {
	
	//此数据是读取的配置文件
	public static String HTML_LOCATION;//生成检测报告html的目录位置

	
	//注入
	 @Autowired(required = false)
	 public void setLoation(@Value("${html.location}")String location) {
		 ConstantConfig.HTML_LOCATION = location;
	 }
}
