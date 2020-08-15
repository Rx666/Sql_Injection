package com.jsql.view.scan.interaction;

import java.util.List;
import java.util.UUID;

import com.jsql.mapper.UrlMapper;
import com.jsql.model.bean.SubUrl;
import com.jsql.model.bean.Url;
import com.jsql.util.SpringContextUtils;
import com.jsql.view.interaction.InteractionCommand;


/**
 * 更新注入状态到数据库中
 * @author 12655
 *
 */
public class UpdateInjectionStatusToDB implements InteractionCommand {
	
	UrlMapper urlMapper = SpringContextUtils.getBean(UrlMapper.class);
	SubUrl subUrl;
	String type;
    
	public UpdateInjectionStatusToDB(Object[] interactionParams) {
		this.type = (String) interactionParams[0];
		this.subUrl = (SubUrl) interactionParams[1];
	}


    @Override
    public void execute() {
    	List<SubUrl> subUrlList = urlMapper.selectBySubUrl(subUrl.getCrawlUrl());
    	if(subUrlList != null && subUrlList.size() > 0) {
    		urlMapper.updateSubUrlInjectionStatus(subUrl,type);
    	}else {
    		String id = UUID.randomUUID().toString();
    		Url url = new Url();
    		url.setId(id);
    		String accessVector = null;
    		if(subUrl.getCrawlUrl().contains("localhost") || subUrl.getCrawlUrl().contains("LOCALHOST") || subUrl.getCrawlUrl().contains("127.0.0.1")) {
    			accessVector = "LOCAL";
    		}else {
    			accessVector = "REMOTE";
    		}
    		url.setAccessVector(accessVector);
    		url.setAccessComplexity("2");//2代表高
    		url.setUrl(subUrl.getCrawlUrl());
    		if(urlMapper.deleteUrl()>0 && urlMapper.insertUrl(url) >0) {
    			subUrl.setUrlId(id);
        		urlMapper.insertSubUrl(subUrl);
    		}
    		
    	}
    }
    
}
