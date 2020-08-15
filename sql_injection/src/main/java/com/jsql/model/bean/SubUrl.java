package com.jsql.model.bean;

/**
 * t_sub_url数据表映射对象
 * @author 12655
 *
 */
public class SubUrl {

	private String urlId;
	private String crawlUrl;
	private String isInjection;
	private String blindInjection;
	private String errorInjection;
	private String timeInjection;
	private String normalInjection;
	private String remark;
	private Integer bugNum;
	
	public String getUrlId() {
		return urlId;
	}
	public void setUrlId(String urlId) {
		this.urlId = urlId;
	}
	public String getCrawlUrl() {
		return crawlUrl;
	}
	public void setCrawlUrl(String crawlUrl) {
		this.crawlUrl = crawlUrl;
	}
	public String getIsInjection() {
		return isInjection;
	}
	public void setIsInjection(String isInjection) {
		this.isInjection = isInjection;
	}
	public String getBlindInjection() {
		return blindInjection;
	}
	public void setBlindInjection(String blindInjection) {
		this.blindInjection = blindInjection;
	}
	public String getErrorInjection() {
		return errorInjection;
	}
	public void setErrorInjection(String errorInjection) {
		this.errorInjection = errorInjection;
	}
	public String getTimeInjection() {
		return timeInjection;
	}
	public void setTimeInjection(String timeInjection) {
		this.timeInjection = timeInjection;
	}
	public String getNormalInjection() {
		return normalInjection;
	}
	public void setNormalInjection(String normalInjection) {
		this.normalInjection = normalInjection;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Integer getBugNum() {
		return bugNum;
	}
	public void setBugNum(Integer bugNum) {
		this.bugNum = bugNum;
	}
	
	
}
