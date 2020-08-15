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
package com.jsql.view.swing.interaction;

import com.jsql.i18n.I18n;
import com.jsql.model.MediatorModel;
import com.jsql.view.interaction.InteractionCommand;
import com.jsql.view.swing.MediatorGui;
import com.jsql.view.swing.manager.util.StateButton;
import com.jsql.view.swing.panel.PanelAddressBar;

/**
 * End the refreshing of administration page search button.
 */
public class EndScan implements InteractionCommand {
    
    /**
     * @param interactionParams
     */
    public EndScan(Object[] interactionParams) {
        // Do nothing
    }

    @Override
    public void execute() {
//        if (MediatorGui.managerScan() == null) {
//            LOGGER.error("Unexpected unregistered MediatorGui.managerScan() in "+ this.getClass());
//        }
//        
//        MediatorGui.managerScan().restoreButtonText();
//        MediatorGui.managerScan().setButtonEnable(true);
//        MediatorGui.managerScan().hideLoader();
//        MediatorGui.managerScan().setStateButton(StateButton.STARTABLE);
//        
//        MediatorGui.panelAddressBar().getLoader().setVisible(false);
//        MediatorGui.panelAddressBar().getButtonInUrl().setInjectionReady();
//        MediatorGui.panelAddressBar().getButtonInUrl().setToolTipText(I18n.valueByKey("BUTTON_STOPPING_TOOLTIP"));
//        MediatorModel.model().setIsStoppedByUser(true);
//        
//        
    }
    
}
