package com.jsql.view.scan.interaction;

import org.apache.log4j.Logger;

import com.jsql.util.crawl.MyCrawler;
import com.jsql.view.interaction.InteractionCommand;
import com.jsql.view.swing.MediatorGui;

/**
 * 	继续扫描
 */
public class NextScan implements InteractionCommand {
    
    private static final Logger LOGGER = Logger.getRootLogger();
    
    String url;
    /**
     * @param interactionParams
     */
    public NextScan(Object[] interactionParams) {
        this.url = (String) interactionParams[0];
    }

    @Override
    public void execute() {
		MediatorGui.panelAddressBar().getTextFieldAddress().setText(url);
		MediatorGui.tabManagers().refresh("[{\"url\":\"\"}]");
		MyCrawler.setStop(false); 
		MediatorGui.panelAddressBar().getButtonInUrl().doClick();
		LOGGER.trace("Scan:" + url);
    }
    
}
