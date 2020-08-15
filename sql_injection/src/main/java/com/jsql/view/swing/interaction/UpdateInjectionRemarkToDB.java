package com.jsql.view.swing.interaction;

import java.util.UUID;

import com.jsql.mapper.UrlMapper;
import com.jsql.model.bean.SubUrl;
import com.jsql.model.bean.Url;
import com.jsql.util.SpringContextUtils;
import com.jsql.view.interaction.InteractionCommand;


/**
 * 更新注入备注到数据库中
 * @author 12655
 *
 */
public class UpdateInjectionRemarkToDB implements InteractionCommand {
	
	UrlMapper urlMapper = SpringContextUtils.getBean(UrlMapper.class);
	String subUrl;
	String remark;
    
	public UpdateInjectionRemarkToDB(Object[] interactionParams) {
		this.remark = (String) interactionParams[0];
		this.subUrl = (String) interactionParams[1];
	}


    @Override
    public void execute() {
    	urlMapper.addMark(subUrl, remark);
    }
    
}
