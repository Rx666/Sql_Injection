package com.jsql.util.crawl;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.jsql.util.HttpUrlUtil;


/**
 *	 爬虫工具
 * @author 12655
 *
 */
public class MyCrawler {
	
	private static final Logger LOGGER = Logger.getRootLogger();
	
	private static boolean STOP = true;//用于停止爬虫线程的标识
    /**
     * 使用种子初始化URL队列
     *
     * @param seeds
     */
    private void initCrawlerWithSeeds(String[] seeds) {
        for (int i = 0; i < seeds.length; i++) {
            LinkQueue.addUnvisitedUrl(seeds[i]);
        }
    }

    public void crawling(String[] seeds) {
        //定义过滤器,提取以http://news.fudan.edu.cn/的链接
        LinkFilter filter = new LinkFilter() {
            @Override
            public boolean accept(String url) {
            	String realmName = HttpUrlUtil.getRealmName(seeds[0]);
           		if(realmName != null && url.contains(realmName)) {
           			return true;
           		}
                return false;
            }
        };
        //初始化URL队列
        initCrawlerWithSeeds(seeds);

        int count=0;
        //循环条件:待抓取的链接不为空
        while (!LinkQueue.isUnvisitedUrlsEmpty()) {
        	if(STOP == true) {
            	break;
            }
            System.out.println("crawl count:"+(++count));

            //附头URL出队列
            String visitURL = (String) LinkQueue.unVisitedUrlDeQueue();
            
            //该URL放入怩访问的URL中
            LinkQueue.addVisitedUrl(visitURL);
            LOGGER.trace(visitURL);
            //提取出下载网页中的URL
            Set<String> links = HtmlParserTool.extractLinks(visitURL, filter);

            //新的未访问的URL入列
            for (String link : links) {
            	System.out.println("判读未访问的link:"+link+"入列");
                LinkQueue.addUnvisitedUrl(link);
            }
        }
        LOGGER.trace("爬虫结束！");
    }

	public static boolean getStopStatus() {
		return STOP;
	}

	public static void setStop(boolean isStop) {
		STOP = isStop;
	}
    
    

}