package com.jsql.view.swing.progressBar;

import javax.swing.JProgressBar;

import com.jsql.model.MediatorModel;

public class ProgressbarThread extends Thread {



	// 标志线程阻塞情况
	private boolean pause = false;
	
	private JProgressBar progressBar;
	
	private int countValue = 0;
	
	public ProgressbarThread(JProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public int getCountValue() {
		return countValue;
	}
	public void setCountValue(int countValue) {
		this.countValue = countValue;
	}
		

	/**
	 * 设置线程是否阻塞
	 */
	public void pauseThread() {
		this.pause = true;
	}

	/**
	 * 调用该方法实现恢复线程的运行
	 */
	public synchronized void resumeThread() {
		this.pause = false;
		notifyAll();

	}

	/**
	 * 这个方法只能在run 方法中实现，不然会阻塞主线程，导致页面无响应
	 */
	public void onPause() {
		this.pause = true;
	}
	
	private void doSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public synchronized void run() {
		for ( ;countValue <= 100; countValue++) {
			if (pause) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (Thread.currentThread().isInterrupted()) {
				// 处理中断逻辑
				if(MediatorModel.model().isStoppedByUser() == true) {
					progressBar.setValue(0);
				}else {
					progressBar.setValue(100);
				}
				
				return;
			}
			doSleep(1000);
			progressBar.setValue(countValue);
			if (countValue == 49 || countValue== 80) {
				pause = true;
				
			}
			
		}
	}

}