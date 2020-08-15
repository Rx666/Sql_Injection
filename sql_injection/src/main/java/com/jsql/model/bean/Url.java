package com.jsql.model.bean;

/**
 * t_url数据表映射对象
 * @author 12655
 *
 */
public class Url {

	private String id;//主键id
	private String url;//连接名称
	private String database;//数据库名称
	private String accessVector;//攻击途径
	private String accessComplexity;//攻击复杂度
	private String authentication;//是否需要认证
	private String confImpact;//机密性
	private String integImpact;//完整性
	private String availImpact;//可用性
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getAccessVector() {
		return accessVector;
	}
	public void setAccessVector(String accessVector) {
		this.accessVector = accessVector;
	}
	public String getAccessComplexity() {
		return accessComplexity;
	}
	public void setAccessComplexity(String accessComplexity) {
		this.accessComplexity = accessComplexity;
	}
	public String getAuthentication() {
		return authentication;
	}
	public void setAuthentication(String authentication) {
		this.authentication = authentication;
	}
	public String getConfImpact() {
		return confImpact;
	}
	public void setConfImpact(String confImpact) {
		this.confImpact = confImpact;
	}
	public String getIntegImpact() {
		return integImpact;
	}
	public void setIntegImpact(String integImpact) {
		this.integImpact = integImpact;
	}
	public String getAvailImpact() {
		return availImpact;
	}
	public void setAvailImpact(String availImpact) {
		this.availImpact = availImpact;
	}
	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	
	
}
