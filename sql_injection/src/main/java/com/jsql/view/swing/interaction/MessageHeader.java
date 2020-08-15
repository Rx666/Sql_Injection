package com.jsql.view.swing.interaction;

import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.DefaultTableModel;

import com.jsql.model.bean.util.Header;
import com.jsql.model.bean.util.HttpHeader;
import com.jsql.view.interaction.InteractionCommand;
import com.jsql.view.swing.MediatorGui;
import com.jsql.view.swing.scrollpane.JScrollIndicator;

/**
 * 将消息添加到选项卡对应窗口
 */
public class MessageHeader implements InteractionCommand {
    
    // The text to append to the tab
    private String url;
    private String post;
    private String header;
    private String sqlCase;
    private Map<String, String> response;
    private String source;

    Map<String, Object> params;
    /**
     * @param 接收到消息后追加内容
     */
    @SuppressWarnings("unchecked")
    public MessageHeader(Object[] interactionParams) {
        this.params = (Map<String, Object>) interactionParams[0];
        this.url = (String) this.params.get(Header.URL);
        this.post = (String) this.params.get(Header.POST);
        this.header = (String) this.params.get(Header.HEADER);
        this.response = (Map<String, String>) this.params.get(Header.RESPONSE);
        this.source = (String) this.params.get(Header.SOURCE);
        this.sqlCase = (String) this.params.get(Header.CASE);
    }

    @Override
    public void execute() {
        if (MediatorGui.panelConsoles() == null) {
            LOGGER.error("Unexpected unregistered MediatorGui.panelConsoles() in "+ this.getClass());
        }
        
        MediatorGui.panelConsoles().addHeader(new HttpHeader(this.url, this.post, this.header, this.response, this.source));
        
        JViewport viewport = ((JScrollIndicator) MediatorGui.panelConsoles().getNetwork().getLeftComponent()).scrollPane.getViewport();
        JTable table = (JTable) viewport.getView();
        
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        
        try {
            model.addRow(new Object[]{this.sqlCase, this.url, this.response.get("Content-Length"), this.response.get("Content-Type")});
            
            Rectangle rect = table.getCellRect(table.getRowCount() - 1, 0, true);
            Point pt = viewport.getViewPosition();
            rect.translate(-pt.x, -pt.y);
            viewport.scrollRectToVisible(rect);
            
            int tabIndex = MediatorGui.tabConsoles().indexOfTab("Network");
            if (0 <= tabIndex && tabIndex < MediatorGui.tabConsoles().getTabCount()) {
                Component tabHeader = MediatorGui.tabConsoles().getTabComponentAt(tabIndex);
                if (MediatorGui.tabConsoles().getSelectedIndex() != tabIndex) {
                    tabHeader.setFont(tabHeader.getFont().deriveFont(Font.BOLD));
                }
            }
        } catch(NullPointerException | IndexOutOfBoundsException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
}
