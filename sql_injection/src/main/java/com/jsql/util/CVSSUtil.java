package com.jsql.util;

/**
 * CVSS 2.0计算工具
 * 
 * @author 12655
 *
 */
public class CVSSUtil {


	public static final float ACCESS_VECTOR_LOCAL = 0.7f;//攻击途径-本地
	public static final float ACCESS_VECTOR_REMOTE = 1.0f;//攻击途径-远程
	
	public static final float ACCESS_COMPLEXITY_HIGHT = 0.6f;//攻击复杂度-高
	public static final float ACCESS_COMPLEXITY_MIDDLE = 0.8f;//攻击复杂度-中
	public static final float ACCESS_COMPLEXITY_LOW = 1.0f;//攻击复杂度-低
	
	public static final float NEED_AUTHENTICATION = 0.6f;//需要认证
	public static final float NOT_NEDD_AUTHENTICATION = 1.0f;//不需要认证
	
	public static final float CONFIMPACT_NONE = 0;//机密性-不受影响
	public static final float CONFIMPACT_SOME = 0.7f;//机密性-部分
	public static final float CONFIMPACT_ALL = 1.0f;//机密性-完全
	
	public static final float INTEGIMPACT_NONE = 0;//完整性-不受影响
	public static final float INTEGIMPACT_SOME = 0.7f;//完整性-部分
	public static final float INTEGIMPACT_ALL = 1.0f;//完整性-完全
	
	public static final float AVAILIMPACT_NONE = 0;//可用性-不受影响
	public static final float AVAILIMPACT_SOME = 0.7f;//可用性-部分
	public static final float AVAILIMPACT_ALL = 1.0f;//可用性-完全
	
	public static final float WEIGHT = 0.3333f;//平均权重
	
	public static int score(String accessVector,float accessComplexity,boolean authentication,float confImpact,float integImpact,float availImpact) {
		float vector = accessVector.equals("LOCAL")?ACCESS_VECTOR_LOCAL:ACCESS_VECTOR_REMOTE;
		float auth = authentication?NEED_AUTHENTICATION:NOT_NEDD_AUTHENTICATION;
		//float secure = confImpact * WEIGHT + availImpact * WEIGHT + integImpact * WEIGHT;
		return Math.round(10*vector*accessComplexity*auth*((float)(confImpact*0.25)+(float)(integImpact*0.25)+(float)(availImpact*0.5)));
	}
}
