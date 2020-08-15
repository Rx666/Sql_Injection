package com.jsql.util.crawl;

import java.util.LinkedList;

/**
 *  	使用链表实现队列
 * @author 12655
 *
 */
public class Queue {

   
    private LinkedList<Object> queueList = new LinkedList<>();


    //入队列
    public void enQueue(Object object) {
        queueList.addLast(object);
    }

    //出队列
    public Object deQueue() {
        return queueList.removeFirst();
    }

    //判断队列是否为空
    public boolean isQueueEmpty() {
        return queueList.isEmpty();
    }

    //判断队列是否包含ject元素..
    public boolean contains(Object object) {
        return queueList.contains(object);
    }

    //判断队列是否为空
    public boolean empty() {
        return queueList.isEmpty();
    }
    
    //清空队列
    public void clearQueue() {
    	int size=queueList.size();
        for(int i=0;i<size;i++){
        	queueList.poll();//移除元素
         }
    }

}