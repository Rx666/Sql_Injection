package com.jsql.util.crawl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author 12655
 *
 */
public class LinkQueue {
	//用MD5算法查重过滤的的url集合
	private static Set<Object> md5Urls = new HashSet<>();
	// 已经访问的队列
	private static Set<Object> visitedUrls = new HashSet<>();
	// 未访问的队列
	private static Queue unVisitedUrl = new Queue();

	// 获得URL队列
	public static Queue getUnVisitedUrl() {
		return unVisitedUrl;
	}

	public static Set<Object> getVisitedUrl() {
		return visitedUrls;
	}

	// 添加到访问过的URL队列中
	public static void addVisitedUrl(String url) {
		visitedUrls.add(url);
		md5Urls.add(getMD5(url));
	}

	// 删除已经访问过的URL
	public static void removeVisitedUrl(String url) {
		visitedUrls.remove(url);
		md5Urls.remove(getMD5(url));
	}

	// 未访问的URL出队列
	public static Object unVisitedUrlDeQueue() {
		return unVisitedUrl.deQueue();
	}

	// 保证每个URL只被访问一次,url不能为空,同时已经访问的URL队列中不能包含该url,而且因为已经出队列了所未访问的URL队列中也不能包含该url
	public static void addUnvisitedUrl(String url) {
		if (url != null && !url.trim().equals("") && !md5Urls.contains(getMD5(url)) && !unVisitedUrl.contains(url)) {
			unVisitedUrl.enQueue(url);
			System.out.println(url + "入列");
		}

	}
	
	
	// 获得已经访问过的URL的数量
	public static int getVisitedUrlNum() {
		return visitedUrls.size();
	}

	// 判断未访问的URL队列中是否为空
	public static boolean isUnvisitedUrlsEmpty() {
		return unVisitedUrl.empty();
	}

	/**
	 * 	重置- 清除队列元素
	 */
	public static void resetQueue() {
		md5Urls.clear();
		visitedUrls.clear();
		unVisitedUrl.clearQueue();
	}
	
	/**
	 * 	获取url的md5值
	 * @param url
	 * @return
	 */
	private static String getMD5(String url) {
		String result = "";
		try {
			MessageDigest md = MessageDigest.getInstance("md5");
			md.update(url.getBytes());
			byte[] bytes = md.digest();
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes) {
				String str = Integer.toHexString(b & 0xFF);
				if (str.length() == 1) {
					sb.append("0");
				}
				sb.append(str);
			}
			result = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}
}