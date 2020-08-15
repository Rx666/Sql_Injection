package com.jsql.model.bean.util;

import com.jsql.model.injection.method.MethodInjection;

public class CrawlUrl {

	private String url;
	
	private String request;
	
	private String requestType;
	
	private String vendor;
	
	private String header;
	
	private MethodInjection injectionType;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public MethodInjection getInjectionType() {
		return injectionType;
	}

	public void setInjectionType(MethodInjection injectionType) {
		this.injectionType = injectionType;
	}
	
	
	
}
