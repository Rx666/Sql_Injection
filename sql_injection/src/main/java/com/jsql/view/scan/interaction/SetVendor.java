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

import com.jsql.mapper.UrlMapper;
import com.jsql.model.injection.vendor.model.Vendor;
import com.jsql.util.SpringContextUtils;
import com.jsql.view.interaction.InteractionCommand;
import com.jsql.view.swing.MediatorGui;

/**
 * Mark the injection as vulnerable to a blind injection.
 */
public class SetVendor implements InteractionCommand {
	
	UrlMapper urlMapper = SpringContextUtils.getBean(UrlMapper.class);
    
    private Vendor vendor;
    
    private String url;
    
    /**
     * @param interactionParams
     */
    public SetVendor(Object[] interactionParams) {
        this.vendor = (Vendor) interactionParams[0];
        this.url = (String) interactionParams[1];
    }

    @Override
    public void execute() {
        if (MediatorGui.panelAddressBar() == null) {
            LOGGER.error("Unexpected unregistered MediatorGui.panelAddressBar() in "+ this.getClass());
        }
        //显示数据库类型
        MediatorGui.panelAddressBar().getMenuVendor().setText(this.vendor.toString());
        MediatorGui.panelAddressBar().initErrorMethods(this.vendor);
        //更新数据库类型到数据库中
        urlMapper.updateDateBaseByUrl(url, this.vendor.toString());
        
    }
    
}
