package com.jsql.view.swing.interaction;

import java.util.UUID;

import com.jsql.mapper.UrlMapper;
import com.jsql.model.bean.SubUrl;
import com.jsql.model.bean.Url;
import com.jsql.util.SpringContextUtils;
import com.jsql.view.interaction.InteractionCommand;


/**
 * 更新无注入状态到数据库中
 * @author 12655
 *
 */
public class SetNoInjectionStatusToDB implements InteractionCommand {
	
	UrlMapper urlMapper = SpringContextUtils.getBean(UrlMapper.class);
	String subUrl;

    
	public SetNoInjectionStatusToDB(Object[] interactionParams) {
		this.subUrl = (String) interactionParams[0];
	}


    @Override
    public void execute() {
    	urlMapper.setNoInjectionStatus(subUrl);
    }
    
}
