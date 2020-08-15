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

import com.jsql.view.interaction.InteractionCommand;
import com.jsql.view.swing.MediatorGui;

/**
 * Erase the screen.
 */
public class ResetInterface implements InteractionCommand {
    
    /**
     * @param interactionParams
     */
    public ResetInterface(Object[] interactionParams) {
        // Do nothing
    }

    @Override
    public void execute() {
        if (MediatorGui.frame() == null) {
            LOGGER.error("Unexpected unregistered MediatorGui.frame() in "+ this.getClass());
        }
        
        MediatorGui.frame().resetInterface();
    }
    
}
