package com.jsql;

import java.awt.AWTError;
import java.awt.HeadlessException;

import org.apache.log4j.Logger;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import com.jsql.mapper.UrlMapper;
import com.jsql.model.InjectionModel;
import com.jsql.model.MediatorModel;
import com.jsql.util.CertificateUtil;
import com.jsql.util.SpringContextUtils;
import com.jsql.util.GitUtil.ShowOnConsole;
import com.jsql.view.swing.JFrameView;
import com.jsql.view.swing.MediatorGui;

/**
 * 程序主类
 */
@EnableCaching
@SpringBootApplication
@MapperScan(value="com.jsql.mapper")
public class MainApplication {
   
    static InjectionModel injectionModel;
    static {
    	//初始化应用配置(注册表)
        injectionModel = new InjectionModel();
        injectionModel.getMediatorUtils().getPreferencesUtil().loadSavedPreferences();
    }
    
    /**
     * 	使用根目录的 log4j.properties配置  /
     */
    private static final Logger LOGGER = Logger.getRootLogger();
    
    
    /**
     * 	程序入口
     */
    public static void main(String[] args) {
    	SpringApplication.run(MainApplication.class, args);
        // 初始化 MVC
        MediatorModel.register(injectionModel);
        
        // 配置全局环境配置
        CertificateUtil.ignoreCertificationChain();
        injectionModel.getMediatorUtils().getExceptionUtil().setUncaughtExceptionHandler();
        injectionModel.getMediatorUtils().getProxyUtil().setProxy();
        injectionModel.getMediatorUtils().getAuthenticationUtil().setKerberosCifs();
        
        try {
            JFrameView view = new JFrameView();
            MediatorGui.register(view);
            
            injectionModel.addObserver(view.getObserver());
            //injectionModel.addObserver(view.getScanObserver());
        } catch (HeadlessException e) {
            LOGGER.error("HeadlessException,不支持jSQL中的命令行执行: "+ e.getMessage(), e);
            return;
        } catch (AWTError e) {
            LOGGER.error("Java Access Bridge 丢失或损坏，请在中检查您的 access bridge 是否在 JDK_HOME/jre/lib/accessibility.properties文件中定义: "+ e.getMessage(), e);
            return;
        }
        
        injectionModel.displayVersion();
        
        
        // 检查应用状态
        if (!injectionModel.getMediatorUtils().getProxyUtil().isLive(ShowOnConsole.YES)) {
            return;
        }
        
    }
    
}
