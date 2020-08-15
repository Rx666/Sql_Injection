package com.jsql.view.swing.tab;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;

import com.jsql.i18n.I18n;
import com.jsql.view.i18n.I18nView;
import com.jsql.view.swing.HelperUi;
import com.jsql.view.swing.MediatorGui;
import com.jsql.view.swing.manager.ManagerScan;
import com.jsql.view.swing.text.JToolTipI18n;
import com.jsql.view.swing.ui.CustomMetalTabbedPaneUI;

/**
 * 	左侧爬虫功能面板。
 */
@SuppressWarnings("serial")
public class TabManagers extends MouseTabbedPane {
    
	 public ManagerScan managerScanList;
    /**
     * 创建标签管理页面
     */
    public TabManagers() {
        this.setUI(new CustomMetalTabbedPaneUI() {
            @Override 
            protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                return Math.max(75, super.calculateTabWidth(tabPlacement, tabIndex, metrics));
            }
        });

        managerScanList = new ManagerScan("[{\"url\":\"\"}]") ;

       
        MediatorGui.register(managerScanList);
        
        this.setMinimumSize(new Dimension(100, 0));
        this.addMouseClickMenu();
        
        this.buildI18nTab("SCANLIST_TAB", "SCANLIST_TOOLTIP", HelperUi.ICON_SCANLIST, managerScanList);
        
    }
    
    /**
     * 重建标签管理页面
     */
    public void refresh(String urlJsonStr) {
    
        this.setUI(new CustomMetalTabbedPaneUI() {
            @Override 
            protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                return Math.max(75, super.calculateTabWidth(tabPlacement, tabIndex, metrics));
            }
        });
        //移除组件
    	if(managerScanList != null) {
    		managerScanList.removeAll();
    		this.remove(managerScanList);
    	}
        //重新创建组件
        managerScanList = new ManagerScan(urlJsonStr);
        MediatorGui.register(managerScanList);

        this.setMinimumSize(new Dimension(100, 0));
        this.addMouseClickMenu();
        this.buildI18nTab("SCANLIST_TAB", "SCANLIST_TOOLTIP", HelperUi.ICON_SCANLIST, managerScanList);
    }
    
    private void buildI18nTab(
        String keyLabel,
        String keyTooltip,
        Icon icon,
        Component manager
    ) {
        final JToolTipI18n[] tooltip = new JToolTipI18n[]{new JToolTipI18n(I18n.valueByKey(keyTooltip))};
        JLabel labelTab = new JLabel("爬取链接", icon, SwingConstants.CENTER){
            @Override
            public JToolTip createToolTip() {
                JToolTip tipI18n = new JToolTipI18n(I18n.valueByKey(keyTooltip));
                tooltip[0] = (JToolTipI18n) tipI18n;
                return tipI18n;
            }
        };
        labelTab.setFont(new Font("MS Song", Font.PLAIN, 10));
        labelTab.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                TabManagers.this.setSelectedComponent(manager);
                super.mousePressed(e);
            }
        });
        this.addTab(I18n.valueByKey(keyLabel), icon, manager);
        this.setTabComponentAt(
            this.indexOfTab(I18n.valueByKey(keyLabel)),
            labelTab
        );
        I18nView.addComponentForKey(keyLabel, labelTab);
        I18nView.addComponentForKey(keyTooltip, tooltip[0]);
        labelTab.setToolTipText(I18n.valueByKey(keyTooltip));
        labelTab.addMouseListener(new TabSelectionMouseHandler());
    }  
    
}
