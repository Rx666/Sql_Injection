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
package com.jsql.view.scan.interaction;

import org.apache.log4j.Logger;

import com.jsql.i18n.I18n;
import com.jsql.view.interaction.InteractionCommand;
import com.jsql.view.swing.MediatorGui;
import com.jsql.view.swing.panel.PanelAddressBar;
import com.jsql.view.swing.panel.SplitHorizontalTopBottom;

/**
 * End the refreshing of administration page search button.
 */
public class EndScan implements InteractionCommand {
    
    private static final Logger LOGGER = Logger.getRootLogger();
    
    /**
     * @param interactionParams
     */
    public EndScan(Object[] interactionParams) {
        // Do nothing
    }

    @Override
    public void execute() {
        MediatorGui.managerScan().restoreButtonText();
        MediatorGui.managerScan().setButtonEnable(true);
        MediatorGui.managerScan().hideLoader();
        MediatorGui.panelAddressBar().getLoader().setVisible(false);
        MediatorGui.panelAddressBar().getButtonInUrl().setInjectionReady();
        MediatorGui.panelAddressBar().getButtonInUrl().setToolTipText(I18n.valueByKey("BUTTON_STOPPING_TOOLTIP"));
        PanelAddressBar addressBar = MediatorGui.panelAddressBar();
        addressBar.getProgressThread().interrupt();
        LOGGER.trace("Scan finished");
    }
    
}
