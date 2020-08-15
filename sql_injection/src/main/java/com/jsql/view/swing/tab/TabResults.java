package com.jsql.view.swing.tab;

import javax.swing.JTabbedPane;
import javax.swing.TransferHandler;

import com.jsql.view.swing.action.ActionHandler;
import com.jsql.view.swing.tab.dnd.DnDTabbedPane;
import com.jsql.view.swing.tab.dnd.TabTransferHandler;

/**
 * 	结果面板
 */
@SuppressWarnings("serial")
public class TabResults extends DnDTabbedPane {
    
    /**
     * 	创建包含注入结果的面板
     */
    public TabResults() {
        this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        TransferHandler handler = new TabTransferHandler();
        this.setTransferHandler(handler);

        // Add hotkeys to rootpane ctrl-tab, ctrl-shift-tab, ctrl-w
        ActionHandler.addShortcut(this);
    }
    
}
