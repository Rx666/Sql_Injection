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

/**
 * End the refreshing of the main Start injection button.
 */
public class EndPreparation implements InteractionCommand {
    
    /**
     * @param interactionParams
     */
    public EndPreparation(Object[] interactionParams) {
        // Do nothing
    }

    @Override
    public void execute() {
        if (MediatorGui.panelAddressBar() == null) {
            LOGGER.error("Unexpected unregistered MediatorGui.panelAddressBar() in "+ this.getClass());
        }
        
        MediatorGui.panelAddressBar().getButtonInUrl().setToolTipText(I18n.valueByKey("BUTTON_START_TOOLTIP"));
        MediatorGui.panelAddressBar().getButtonInUrl().setInjectionReady();
        MediatorGui.panelAddressBar().getLoader().setVisible(false);

		/*
		 * if (MediatorModel.model().isInjectionAlreadyBuilt()) {
		 * MediatorGui.managerFile().setButtonEnable(true);
		 * MediatorGui.managerWebshell().setButtonEnable(true);
		 * MediatorGui.managerSqlshell().setButtonEnable(true);
		 * MediatorGui.managerUpload().setButtonEnable(true); }
		 */
    }
    
}
