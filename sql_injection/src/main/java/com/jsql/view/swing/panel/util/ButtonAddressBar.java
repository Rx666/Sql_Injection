package com.jsql.view.swing.panel.util;

import java.awt.Dimension;

import javax.swing.JButton;

import com.jsql.view.swing.HelperUi;
import com.jsql.view.swing.manager.util.StateButton;

/**
 * 地址连接按钮（—>）
 */
@SuppressWarnings("serial")
public class ButtonAddressBar extends JButton {
    
    /**
     * 	当前的检查状态
     */
    private StateButton state = StateButton.STARTABLE;
    
    /**
     * 	创建按钮
     */
    public ButtonAddressBar() {
        this.setPreferredSize(new Dimension(18, 16));
        this.setOpaque(false);
        this.setContentAreaFilled(false);
        this.setBorderPainted(false);
        
        // 设置翻转效果
        this.setRolloverEnabled(true);
        this.setIcon(HelperUi.ICON_ARROW_DEFAULT);
        this.setRolloverIcon(HelperUi.ICON_ARROW_ROLLOVER);
        this.setPressedIcon(HelperUi.ICON_ARROW_PRESSED);
    }

    /**
     * 	返回当前进程的当前状态
     * @return State of process
     */
    public StateButton getState() {
        return this.state;
    }

    /**
     * 	用方向图标替换按钮; 用户可以停止当前进程。
     */
    public void setInjectionReady() {
        this.state = StateButton.STARTABLE;//设置为可开始状态
        this.setEnabled(true);
        
        // 设置翻转效果
        this.setRolloverEnabled(true);
        this.setIcon(HelperUi.ICON_ARROW_DEFAULT);
        this.setRolloverIcon(HelperUi.ICON_ARROW_ROLLOVER);
        this.setPressedIcon(HelperUi.ICON_ARROW_PRESSED);
    }

    /**
     * 	用停止图标替换按钮; 用户可以停止当前进程。
     */
    public void setInjectionRunning() {
        this.state = StateButton.STOPPABLE;//设置为可停止状态
        this.setEnabled(true);
        
        // 设置翻转效果
        this.setRolloverEnabled(true);
        this.setIcon(HelperUi.IMG_STOP_DEFAULT);
        this.setRolloverIcon(HelperUi.IMG_STOP_ROLLOVER);
        this.setPressedIcon(HelperUi.IMG_STOP_PPRESSED);
    }

    /**
	 *	用动画GIF替换按钮，直到注入过程完成; 用户等待过程结束。
     */
    public void setInjectionStopping() {
        this.state = StateButton.STOPPING;//设置停止状态
        
        // 设置翻转效果
        this.setRolloverEnabled(false);
        this.setIcon(HelperUi.ICON_LOADER_GIF);
        this.setEnabled(false);
    }
    
}
