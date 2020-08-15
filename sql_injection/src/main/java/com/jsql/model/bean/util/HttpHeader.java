package com.jsql.model.bean.util;

import java.util.Map;

/**
 * An HTTP object containing request and response data.
 */
public class HttpHeader {
    
    /**
     * GET request.
     */
    private String url;
    
    /**
     * POST request.
     */
    private String post;
    
    /**
     * Header request.
     */
    private String header;
    
    /**
     * Header sent back by URL.
     */
    private Map<String, String> response;
    
    private String source;
    
    /**
     * Create object containing HTTP data to display in Network panel.
     * @param url URL called
     * @param post POST text sent with url
     * @param header HEADER text sent with url
     * @param response RESPONSE header sent by url
     */
    public HttpHeader(String url, String post, String header,
            Map<String, String> response, String source) {
        this.url = url;
        this.post = post;
        this.header = header;
        this.response = response;
        this.source = source;
    }
    
    /**
     * Get GET request.
     * @return GET text
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Get POST request.
     * @return POST text
     */
    public String getPost() {
        return this.post;
    }

    /**
     * Get header request.
     * @return Header text
     */
    public String getHeader() {
        return this.header;
    }

    /**
     * Get response from the server.
     * @return Response source code
     */
    public Map<String, String> getResponse() {
        return this.response;
    }

    @Override
    public String toString() {
        return this.url;
    }

    public String getSource() {
        return this.source;
    }
    
}
