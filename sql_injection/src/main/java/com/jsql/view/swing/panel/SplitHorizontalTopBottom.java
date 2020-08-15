package com.jsql.view.swing.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;

import com.jsql.model.InjectionModel;
import com.jsql.view.swing.HelperUi;
import com.jsql.view.swing.MediatorGui;
import com.jsql.view.swing.panel.util.ActionHideShowConsole;
import com.jsql.view.swing.panel.util.ActionHideShowResult;
import com.jsql.view.swing.splitpane.JSplitPaneWithZeroSizeDivider;
import com.jsql.view.swing.tab.TabManagers;
import com.jsql.view.swing.tab.TabResults;

/**
 * SplitPane由顶部的树和选项卡以及底部的信息选项卡组成。
 */
@SuppressWarnings("serial")
public class SplitHorizontalTopBottom extends JSplitPaneWithZeroSizeDivider {
    
    /**
     * 垂直分割线的名称.
     * 重置分割线的位置.
     */
    private static final String NAME_V_SPLITPANE = "verticalSplitter-";
    
    /**
     * 水平分割线的名称.
     * 重置分割线的位置.
     */
    private static final String NAME_H_SPLITPANE = "horizontalSplitter-";

    /**
     * SplitPane 左侧管理器面板，右侧结果选项卡。
     */
    private JSplitPaneWithZeroSizeDivider splitVerticalLeftRight;

    private static final JPanel PANEL_HIDDEN_CONSOLES = new JPanel();
    
    /**
     * 	用于Tabbedpane标头上的箭头和上的MouseAdapter隐藏底部面板时的ersatz按钮。
     */
    private static final ActionHideShowConsole ACTION_HIDE_SHOW_CONSOLE = new ActionHideShowConsole(PANEL_HIDDEN_CONSOLES);
    private static final ActionHideShowResult ACTION_HIDE_SHOW_RESULT= new ActionHideShowResult();

    private final JLabel labelPlaceholderResult;
        
    /**
     * 进度条
     */
    private static JProgressBar progress;
    
    /**
     *	 使用左侧的管理器面板，右侧的结果选项卡创建主面板，和控制台在底部。
     */
    public SplitHorizontalTopBottom() {
        super(JSplitPane.VERTICAL_SPLIT);

        Preferences prefs = Preferences.userRoot().node(InjectionModel.class.getName());
        int verticalSplitter = prefs.getInt(SplitHorizontalTopBottom.NAME_V_SPLITPANE, 350);
        int horizontalSplitter = prefs.getInt(SplitHorizontalTopBottom.NAME_H_SPLITPANE, 200);

        MediatorGui.register(new TabManagers());
        MediatorGui.register(new TabResults());

        // 头部标签
        this.splitVerticalLeftRight = new JSplitPaneWithZeroSizeDivider(JSplitPane.HORIZONTAL_SPLIT);
        this.splitVerticalLeftRight.setLeftComponent(MediatorGui.tabManagers());
        
        this.labelPlaceholderResult = new JLabel(HelperUi.IMG_BUG);
        this.labelPlaceholderResult.setMinimumSize(new Dimension(100, 0));

        this.labelPlaceholderResult.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.labelPlaceholderResult.setAlignmentY(Component.CENTER_ALIGNMENT);        
    	      
        this.splitVerticalLeftRight.setRightComponent(this.labelPlaceholderResult);
        this.splitVerticalLeftRight.setDividerLocation(verticalSplitter);
        this.splitVerticalLeftRight.setDividerSize(0);
        this.splitVerticalLeftRight.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, HelperUi.COLOR_COMPONENT_BORDER));

        this.setDividerSize(0);
        this.setBorder(null);

        JPanel panelManagerResult = new JPanel(new BorderLayout());
        panelManagerResult.add(this.splitVerticalLeftRight, BorderLayout.CENTER);
        //进度条
        progress = new JProgressBar(0,100);
        progress.setStringPainted(true);
     
    	panelManagerResult.add(progress, BorderLayout.NORTH);

        PANEL_HIDDEN_CONSOLES.setLayout(new BorderLayout());
        PANEL_HIDDEN_CONSOLES.setOpaque(false);
        PANEL_HIDDEN_CONSOLES.setPreferredSize(new Dimension(17, 22));
        PANEL_HIDDEN_CONSOLES.setMaximumSize(new Dimension(17, 22));
        JButton buttonShowConsoles = new BasicArrowButton(SwingConstants.NORTH);
        buttonShowConsoles.setBorderPainted(false);
        buttonShowConsoles.setOpaque(false);

        buttonShowConsoles.addActionListener(SplitHorizontalTopBottom.ACTION_HIDE_SHOW_CONSOLE);
        PANEL_HIDDEN_CONSOLES.add(Box.createHorizontalGlue());
        PANEL_HIDDEN_CONSOLES.add(buttonShowConsoles, BorderLayout.LINE_END);
        PANEL_HIDDEN_CONSOLES.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, HelperUi.COLOR_COMPONENT_BORDER));
        PANEL_HIDDEN_CONSOLES.setVisible(false);

        panelManagerResult.add(PANEL_HIDDEN_CONSOLES, BorderLayout.SOUTH);

        //设置顶部和底部组件
        this.setTopComponent(panelManagerResult);

        MediatorGui.register(new PanelConsoles());

        this.setBottomComponent(MediatorGui.panelConsoles());
        this.setDividerLocation(671 - horizontalSplitter);

        // 定义左窗格和底部面板
        this.setResizeWeight(1);
    }
    
    // Getter and setter

    public static String getNameVSplitpane() {
        return NAME_V_SPLITPANE;
    }

    public static String getNameHSplitpane() {
        return NAME_H_SPLITPANE;
    }

    public JSplitPaneWithZeroSizeDivider getSplitVerticalLeftRight() {
        return this.splitVerticalLeftRight;
    }

    public static ActionHideShowConsole getActionHideShowConsole() {
        return ACTION_HIDE_SHOW_CONSOLE;
    }
    
    public static ActionHideShowResult getActionHideShowResult() {
        return ACTION_HIDE_SHOW_RESULT;
    }

    public JLabel getLabelPlaceholderResult() {
        return this.labelPlaceholderResult;
    }
    
    public static JProgressBar getProgressBar() {
    	return progress;
    }
    
}
