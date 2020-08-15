package com.jsql.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.jsql.model.bean.SubUrl;
import com.jsql.model.bean.Url;


/**
 * 数据库访问mapper映射接口
 * @author 12655
 *
 */
public interface UrlMapper {
 
	Url selectByUrl(@Param("url")String url);
	
	int insertUrl(@Param("url")Url url);
	
	int deleteUrl();
	
	int deleteSubUrlByUrlId(@Param("urlId")String urlId);
	
	int batchInsertSubUrl(@Param("list")List<SubUrl> list);
	
	List<SubUrl> selectBySubUrl(@Param("subUrl")String subUrl);
	
	int updateSubUrlInjectionStatus(@Param("subUrl")SubUrl subUrl,@Param("type")String type);
	
	int insertSubUrl(@Param("subUrl")SubUrl subUrl);
	
	List<SubUrl> selectSubUrlListById(@Param("urlId")String urlId);
	
	List<Url> selectUrlAll();
	
	int setNoInjectionStatus(@Param("subUrl")String subUrl);
	
	int updateAuthenticationStatus(@Param("id")String id);
	
	float countConfImpactById(@Param("urlId")String urlId);
	
	int addMark(@Param("url")String subUrl,@Param("remark")String remark);
	
	int addBugNum(@Param("url")String subUrl);

	void updateDateBaseByUrl(@Param("url")String url, @Param("database")String dataBase);

	int selectCountBugNumById(@Param("id")String id);
}