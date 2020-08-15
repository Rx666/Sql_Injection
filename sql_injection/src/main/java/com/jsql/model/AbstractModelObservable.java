/*******************************************************************************
 * Copyhacked (H) 2012-2016.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss about it
 * every time possible with every body.
 * 
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 ******************************************************************************/
package com.jsql.model;

import java.util.Observable;

import javax.swing.SwingUtilities;

import com.jsql.model.bean.util.Request;

/**
  *	定义注射模型的特征：<br>
  *	-停止准备注入，<br>
  *	-可用于并行化HTTP任务，<br>
  *	-通过Observable与视图通信。
 */
public abstract class AbstractModelObservable extends Observable {
    /**
     * 如果用户要停止准备，则为true。<br>
      *在准备过程中，几种方法将
      *检查是否必须停止执行。
     */
    private boolean isStoppedByUser = false;
    
    private boolean isBatchScan = false;

    /**
     *  inject（）方法的函数标头，call（）所需的定义，
      * dataInjection：SQL查询，
      * responseHeader未使用，
      * useVisibleIndex如果不需要注入索引，则为false，
      * HTTP调用后返回源页面。
     */
    public abstract String inject(String dataInjection, boolean isUsingIndex);
    
    /**
     * 如“ select 1,2，...”中那样无需索引即可进行注入<br>
      *例如，用于：第一个索引测试（getVisibleIndex），错误测试以及错误，盲目，时间策略。
     * @return source code of current page
     */
    public String injectWithoutIndex(String dataInjection) {
        return this.inject(dataInjection, false);
    }

    public String injectWithIndexes(String dataInjection) {
        return this.inject(dataInjection, true);
    }

    /**
     * 发送交互消息到注册视图。
     * @param interaction The event bean corresponding to the interaction
     */
    public void sendToViews(final Request interaction) {
        // Display model thread name in logs instead of the observer name
        String nameThread = Thread.currentThread().getName();
        SwingUtilities.invokeLater(() -> {
            Thread.currentThread().setName("from " + nameThread);
            AbstractModelObservable.this.setChanged();
            AbstractModelObservable.this.notifyObservers(interaction);
        });
    }

    // Getters and setters
    
    public boolean isStoppedByUser() {
        return this.isStoppedByUser;
    }

    public void setIsStoppedByUser(boolean processStopped) {
        this.isStoppedByUser = processStopped;
    }

	public boolean isBatchScan() {
		return this.isBatchScan;
	}

	public void setBatchScan(boolean isBatchScan) {
		this.isBatchScan = isBatchScan;
	}
    
    
    
}
