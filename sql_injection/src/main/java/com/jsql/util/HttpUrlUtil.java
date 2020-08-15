package com.jsql.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * http链接相关工具
 * @author 12655
 *
 */
public class HttpUrlUtil {

    /**
     * 	获取url的域名(截取 http://xxxx/)
     * @param url
     * @return
     */
    public static String getRealmName(String url) {
    	String realmName = null;
    	String pattern1 = "[hH][tT]{2}[pP][Ss]{0,1}://.*";
   		if(Pattern.matches(pattern1, url)) {
   			 String pattern2 = "[hH][tT]{2}[pP][Ss]{0,1}://";
  			 Pattern p = Pattern.compile(pattern2);
  			 Matcher m = p.matcher(url);
  			 if(m.find()) {//包含单位的
  				String url2 = url.replace(m.group(), "");
  				String pattern3 = "([A-Za-z0-9]+[.]{0,1})+\\/([A-Za-z0-9]+[\\/]{0,1}){0,1}";
  	  			Pattern p2 = Pattern.compile(pattern3);
  	  			Matcher m2 = p2.matcher(url2);
  	  			if(m2.find()) {
  	  				String url3 = m2.group();
  	  				realmName = url3.substring(0,url3.indexOf("/"));
  	  			}else {
  	  			realmName = url2;
  	  			}
  			 }
   		 }
   		return realmName;
    }
    

    /**
     * 	判断字符串是否为URL
     * @param urls 需要判断的String类型url
     * @return true:是URL；false:不是URL
     */

    public static boolean isHttpUrl(String urls) {
//        String regex = "(((https|http)://)([a-z0-9]+[.])|(www.))"
//            + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";//设置正则表达式
    	//包含localhost的
    	String regex = "((https|http)://)(((([a-z0-9]+[.])|(www.))"
              + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+)|(localhost:[0-9]+))((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";//设置正则表达式
    	Pattern pattern = Pattern.compile(regex.trim());//对比
        Matcher match = pattern.matcher(urls.trim());
        return match.matches();
    }
    
}
