package com.jsql.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.jsql.model.InjectionModel;


/**
 * 	读取 config.properties配置文件工具栏
 * @author 12655
 *
 */
public class PropertiesUtil {
    

    private static final Logger LOGGER = Logger.getRootLogger();

    private final Properties properties = new Properties();
    
    InjectionModel injectionModel;
    public PropertiesUtil(InjectionModel injectionModel) {
        this.injectionModel = injectionModel;

        InputStream input = null;

        try {

            String filename = "config.properties";
            input = PropertiesUtil.class.getClassLoader().getResourceAsStream(filename);
            if (input == null) {
                LOGGER.warn("Properties file "+ filename +" not found");
                return;
            }
            this.getProperties().load(new InputStreamReader(input,"utf-8"));

        } catch (IOException e) {
            LOGGER.error(e, e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOGGER.error(e, e);
                }
            }
        }
        
    }

    public Properties getProperties() {
        return this.properties;
    }

}
