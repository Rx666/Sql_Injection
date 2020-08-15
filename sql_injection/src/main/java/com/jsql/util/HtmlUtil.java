package com.jsql.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.jsql.config.ConstantConfig;
import com.jsql.model.bean.SubUrl;
import com.jsql.model.bean.Url;

/**
 * html工具
 * 
 * @author 12655
 *
 */
public class HtmlUtil {
	
	public synchronized static void exportHtml(Url url, List<SubUrl> subUrlList,int score,int bugNum) throws IOException {
		PrintStream printStream;
		File dir = new File(ConstantConfig.HTML_LOCATION + "report");
		if(!dir.exists()) {
			dir.mkdir();
		}
		OutputStream fileOutStream = new FileOutputStream(dir+ File.separator + "report_"+url.getId()+".html");
		printStream = new PrintStream(fileOutStream);
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>sql检测报告</title>");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
		sb.append("<style type=\"text/css\">");
		sb.append(
				"TABLE{border-collapse:collapse;border-left:solid 1 #000000; border-top:solid 1 #000000;padding:5px;}");
		sb.append("TH{border-right:solid 1 #000000;border-bottom:solid 1 #000000;}");
		sb.append("TD{font:normal;border-right:solid 1 #000000;border-bottom:solid 1 #000000;}");
		sb.append("</style></head>");
		sb.append("<body bgcolor=\"#FFF8DC\">");
		sb.append("<h1 align='center'>sql注入检测报告</h1>");
		sb.append("<br/>");
		sb.append("<h5>链接：" + url.getUrl() + " &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;安全评价：" + score + "</h5>");
		sb.append("<div align=\"center\">");
		sb.append("<table border=\"1\">");
		sb.append("<tr>");
		sb.append("<th style='width:150px;'>攻击途径：</th>");
		sb.append("<td style='width:150px;text-align:center;'>"+(url.getAccessVector().equals("LOCAL")?"本地":"远程")+"</td>");
		sb.append("<th style='width:150px;'>数据库类型</th>");
		sb.append("<td style='width:150px;text-align:center;'>"+url.getDatabase()+"</td>");
		sb.append("<th style='width:150px;'>需要认证</th>");
		sb.append("<td style='width:150px;text-align:center;'>"+(url.getAuthentication().equals("0")?"不需要":"需要")+"</td>");
		sb.append("<th style='width:150px;'>复杂程度</th>");
		sb.append("<td style='width:150px;text-align:center;'>一般</td>");
		sb.append("<th style='width:150px;'>漏洞数量</th>");
		sb.append("<td style='width:150px;text-align:center;'>"+bugNum+"</td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("<br/>");
		sb.append("<table border=\"1\">");
		sb.append("<tr>");
		sb.append("<th>链接</th>");
		sb.append("<th style='width:150px;'>检测结果</th>");
		sb.append("<th style='width:150px;'>盲注</th>");
		sb.append("<th style='width:150px;'>错误注入</th>");
		sb.append("<th style='width:150px;'>时间注入</th>");
		sb.append("<th style='width:150px;'>正常注入</th>");
		sb.append("<th>备注</th>");
		sb.append("</tr>");
		if (subUrlList != null) {
			for (SubUrl subUrl : subUrlList) {
				sb.append("<tr>");
				sb.append("<td>");
				sb.append(subUrl.getCrawlUrl());
				sb.append("</td>");
				if (subUrl.getIsInjection() == null) {
					sb.append("<td style='text-align:center;'><font color='blue'>未检测</font></td>");
					sb.append("<td></td>");
					sb.append("<td></td>");
					sb.append("<td></td>");
					sb.append("<td></td>");
					sb.append("<td></td>");
				} else if (subUrl.getIsInjection().equals("0")) {
					sb.append("<td style='text-align:center;'><font color='green'>安全</font></td>");
					sb.append("<td></td>");
					sb.append("<td></td>");
					sb.append("<td></td>");
					sb.append("<td></td>");
					sb.append("<td></td>");
				} else {
					sb.append("<td style='text-align:center;'>");
					sb.append("存在"+subUrl.getBugNum()+"漏洞");
					sb.append("</td>");
					sb.append("<td style='text-align:center;'>");
					sb.append(subUrl.getBlindInjection().equals("1") ? "&radic;" : "");
					sb.append("</td>");
					sb.append("<td style='text-align:center;'>");
					sb.append(subUrl.getErrorInjection().equals("1") ? "&radic;" : "");
					sb.append("</td>");
					sb.append("<td style='text-align:center;'>");
					sb.append(subUrl.getTimeInjection().equals("1") ? "&radic;" : "");
					sb.append("</td>");
					sb.append("<td style='text-align:center;'>");
					sb.append(subUrl.getNormalInjection().equals("1") ? "&radic;" : "");
					sb.append("</td>");
					sb.append("<td style='text-align:center;'>");
					sb.append(subUrl.getRemark());
					sb.append("</td>");
					sb.append("</tr>");
				}
			}
		}
		sb.append("</table>");
		sb.append("<br/>");
		sb.append("<p><font color='red'>评分标准：</font>危险等级低-[0,4);  危险等级中-[4,4);  危险等级高-[7,10]</p>");
		sb.append("<br/>");
		sb.append("</div></body></html>");
		printStream.println(sb.toString());
		fileOutStream.close();
		printStream.close();
	}
	
	

}
