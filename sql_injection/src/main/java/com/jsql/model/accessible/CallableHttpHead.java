package com.jsql.model.accessible;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Logger;

import com.jsql.model.InjectionModel;
import com.jsql.model.bean.util.Header;
import com.jsql.model.bean.util.Interaction;
import com.jsql.model.bean.util.Request;
import com.jsql.util.HeaderUtil;

/**
 * 测试服务器上是否存在管理页面的线程单元,该过程可以由用户取消。
 */
public class CallableHttpHead implements Callable<CallableHttpHead> {
    
    private static final Logger LOGGER = Logger.getRootLogger();
    
    /**
     * 网站上要进行测试的管理页面的URL。
     */
    private String urlAdminPage;
    
    /**
     *HTTP标头响应代码。
     */
    private String responseCodeHttp = "";

    /**
     * 创建一个可调用的查找管理员页面。
     * @param urlAdminPage URL of admin page
     */
    public CallableHttpHead(String urlAdminPage, InjectionModel injectionModel) {
        this.urlAdminPage = urlAdminPage;
        this.injectionModel= injectionModel;
    }
    InjectionModel injectionModel;

    /**
     * 在HEAD模式下将URL调用到管理页面，然后将结果发送回查看。
     */
    @Override
    public CallableHttpHead call() throws Exception {
        boolean isUrlIncorrect = false;
        
        URL targetUrl = null;
        try {
            targetUrl = new URL(this.urlAdminPage);
        } catch (MalformedURLException e) {
            isUrlIncorrect = true;
        }
        
        if (
                this.injectionModel.getResourceAccess().isSearchAdminStopped()
            || isUrlIncorrect
            || "".equals(targetUrl.getHost())
        ) {
            LOGGER.warn("URL格式错误: "+ this.urlAdminPage);
            return this;
        }
            
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        
        connection.setRequestProperty("Pragma", "no-cache");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Expires", "-1");
        
        connection.setRequestMethod("HEAD");
        this.responseCodeHttp = ObjectUtils.firstNonNull(connection.getHeaderField(0), "");

        Map<Header, Object> msgHeader = new EnumMap<>(Header.class);
        msgHeader.put(Header.URL, this.urlAdminPage);
        msgHeader.put(Header.POST, "");
        msgHeader.put(Header.HEADER, "");
        msgHeader.put(Header.RESPONSE, HeaderUtil.getHttpHeaders(connection));

        Request request = new Request();
        request.setMessage(Interaction.MESSAGE_HEADER);
        request.setParameters(msgHeader);
        this.injectionModel.sendToViews(request);
        
        return this;
    }

    /**
     * 检查HTTP响应是2xx还是3xx，它们对应于来自网站的可接受答复。
     * @return true if HTTP code start with 2 or 3
     */
    public boolean isHttpResponseOk() {
        return this.responseCodeHttp.matches(".+[23]\\d\\d.+");
    }
    
    // Getters and setters
    
    public String getUrl() {
        return this.urlAdminPage;
    }
    
}