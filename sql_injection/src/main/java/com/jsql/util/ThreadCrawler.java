package com.jsql.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * 多线程爬虫
 */
public class ThreadCrawler implements Runnable {
    // 采集的文章数
    private final AtomicLong pageCount = new AtomicLong(0);
    // 列表页链接正则表达式
    //public static final String URL_LIST = "http://localhost:8081/user/index";
    protected Logger logger = Logger.getRootLogger();
    // 待采集的队列
    LinkedBlockingQueue<String> taskQueue;
    // 采集过的链接列表
    HashSet<String> visited;
    // 线程池
    CountableThreadPool threadPool;
    /**
     *
     * @param url 起始页
     * @param threadNum 线程数
     * @throws InterruptedException
     */
    public ThreadCrawler(String url, int threadNum) throws InterruptedException {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.threadPool = new CountableThreadPool(threadNum);
        this.visited = new HashSet<>();
        // 将起始页添加到待采集队列中
        this.taskQueue.put(url);
    }

    @Override
    public void run() {
        logger.info("Spider started!");
        while (!Thread.currentThread().isInterrupted()) {
            // 从队列中获取待采集 URL
            final String url = taskQueue.poll();
            // 如果获取 request 为空，并且当前的线程采已经没有线程在运行
            if (url == null) {
                if (threadPool.getThreadAlive() == 0) {
                    break;
                }
            } else {
                // 执行采集任务
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                        	 processPage(url);
                        } catch (Exception e) {
                            logger.error("process request " + url + " error", e);
                        } finally {
                            // 采集页面 +1
                            pageCount.incrementAndGet();
                        }
                    }
                });
            }
        }
        threadPool.shutdown();
        logger.info("Spider closed! "+pageCount.get()+" pages downloaded." );
    }

    /**
     * 	解析页面
     * 	处理链接采集
     * 	处理列表页，将 url 添加到队列中
     *
     * @param url
     */
    protected void processPage(String url) {
        try {
        	//解析页面
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.getElementsByTag("a");//获取a标签
            elements.stream().forEach((element -> {
                String link = element.attr("href");
                String regex = "^(?:https?://)?[\\w]{1,}(?:\\.?[\\w]{1,})+[\\w-_/?&=#%:]*$";
            	Pattern pattern = Pattern.compile(regex);
            	Matcher matcher = pattern.matcher(link);
                // 判断该链接是否存在队列或者已采集的 set 中，不存在则添加到队列中
                if (matcher.find()) {
                    try {
                    	System.out.println(matcher.group());
                        taskQueue.put(matcher.group());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public static void main(String[] args) {

        try {
            new ThreadCrawler("https://blog.csdn.net/chang995196962/article/details/83376467", 5).run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}