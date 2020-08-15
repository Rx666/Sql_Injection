package com.jsql.model.bean.excel;

import com.jsql.util.excel.ExcelColumn;

/**
 * excel链接对象
 * @author 12655
 *
 */
public class ExcelUrlBean {

	@ExcelColumn(value = "url", col = 1)
	private String url;

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	
}
