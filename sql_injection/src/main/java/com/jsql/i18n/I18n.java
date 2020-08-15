package com.jsql.i18n;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
  *	实用程序类，可管理不同的文本翻译，例如英语，中文。
 */
public class I18n {
    
    private static final Logger LOGGER = Logger.getRootLogger();
    
    
    /**
     * 捆绑了标准i18n密钥和root语言英语的翻译文本
     */
    private static final ResourceBundle LOCALE_ROOT = ResourceBundle.getBundle("com.jsql.i18n.jsql", Locale.ROOT);
    
    /**
     * 捆绑的i18n键和当前系统语言的翻译文本
     */
    private static ResourceBundle localeDefault = ResourceBundle.getBundle("com.jsql.i18n.jsql", Locale.getDefault());
    
    // Utility class
    private I18n() {
        // not used
    }
    
    /**
     * 返回属性中与i18n键对应的文本。
     * @param key a i18n key in the properties
     * @return text corresponding to the key
     */
    public static String valueByKey(String key) {
        return (String) I18n.localeDefault.getObject(key);
    }
    
    /**
     * 验证是否存在与当前系统语言相对应的语言属性文件。如果没有，则用户使用翻译过程。
     * @throws URISyntaxException
     */
    public static void checkCurrentLanguage() {
        URL path = I18n.class.getResource("/com/jsql/i18n/jsql_"+ Locale.getDefault().getLanguage() +".properties");
        if (!"en".equals(Locale.getDefault().getLanguage()) && path == null) {
            String languageHost = Locale.getDefault().getDisplayLanguage(Locale.ENGLISH);
            LOGGER.debug(
                "Language "+ languageHost +" is not supported, "
                + "please contribute and translate pieces of jSQL into "+ languageHost +": "
                + "click on the top right button and open menu [Community], choose [I help translate jSQL into][another language...] and "
                + "translate some text into "+ languageHost +" then click on [Send]. Your translation will be integrated to the next release by the developer."
            );
        }
    }
    
    // Getters and setters
    
    public static void setLocaleDefault(ResourceBundle localeDefault) {
        I18n.localeDefault = localeDefault;
    }
    
    public static Locale getLocaleDefault() {
        return I18n.localeDefault.getLocale();
    }

    public static ResourceBundle getLocaleRoot() {
        return LOCALE_ROOT;
    }
    
}
