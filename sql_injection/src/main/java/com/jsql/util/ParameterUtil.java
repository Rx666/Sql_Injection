package com.jsql.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.jsql.model.InjectionModel;
import com.jsql.model.exception.InjectionFailureException;

public class ParameterUtil {
    
    /**
     * 	根据用户提交的URL构建的查询字符串。
     */
    private List<SimpleEntry<String, String>> queryString = new ArrayList<>();

    /**
     * 	用户提交的请求。
     */
    private List<SimpleEntry<String, String>> request = new ArrayList<>();
    
    private String requestAsText = "";

    /**
     * 	用户提交的Header 
     */
    private List<SimpleEntry<String, String>> header = new ArrayList<>();

    private InjectionModel injectionModel;
    
    public ParameterUtil(InjectionModel injectionModel) {
        this.injectionModel = injectionModel;
    }

    /**
     *	检测用户定义的参数的完整性。
     * @param isParamByUser 如果未定义注入点，则为true
     * @param parameter 当前从Query / Request / Header注入的值，如果仅测试完整性，则为null
     * @throws InjectionFailureException 
     */
    public void checkParametersFormat() throws InjectionFailureException {
        int nbStarInParameter = 0;
        
        if (this.getQueryStringFromEntries().contains(InjectionModel.STAR)) {
            nbStarInParameter++;
        }
        if (this.getRequestFromEntries().contains(InjectionModel.STAR)) {
            nbStarInParameter++;
        }
        if (this.getHeaderFromEntries().contains(InjectionModel.STAR)) {
            nbStarInParameter++;
        }
        
        // Injection Point
        if (nbStarInParameter >= 2) {
            throw new InjectionFailureException("Character * must be used once in Query String, Request or Header parameters");
            
        } else if (
            this.getQueryStringFromEntries().contains(InjectionModel.STAR)
            && this.injectionModel.getMediatorUtils().getConnectionUtil().getMethodInjection() != this.injectionModel.getMediatorMethodInjection().getQuery()
            && !this.injectionModel.getMediatorUtils().getPreferencesUtil().isCheckingAllParam()
        ) {
            throw new InjectionFailureException("Select method GET to use character [*] or remove [*] from GET parameters");
            
        } else if (
            this.getRequestFromEntries().contains(InjectionModel.STAR)
            && this.injectionModel.getMediatorUtils().getConnectionUtil().getMethodInjection() != this.injectionModel.getMediatorMethodInjection().getRequest()
            && !this.injectionModel.getMediatorUtils().getPreferencesUtil().isCheckingAllParam()
        ) {
            throw new InjectionFailureException("Select a Request method (like POST) to use [*], or remove [*] from Request parameters");
            
        } else if (
            this.getHeaderFromEntries().contains(InjectionModel.STAR)
            && this.injectionModel.getMediatorUtils().getConnectionUtil().getMethodInjection() != this.injectionModel.getMediatorMethodInjection().getHeader()
            && !this.injectionModel.getMediatorUtils().getPreferencesUtil().isCheckingAllParam()
        ) {
            throw new InjectionFailureException("Select method Header to use character [*] or remove [*] from Header parameters");
        }
        
        // 查询字符串
        else if (
            this.injectionModel.getMediatorUtils().getConnectionUtil().getMethodInjection() == this.injectionModel.getMediatorMethodInjection().getQuery()
            && !this.injectionModel.getMediatorUtils().getPreferencesUtil().isCheckingAllParam()
            && this.getQueryString().isEmpty()
            && !this.injectionModel.getMediatorUtils().getConnectionUtil().getUrlBase().contains(InjectionModel.STAR)
        ) {
            throw new InjectionFailureException("无查询参数！");
        }
        
        // Request/Header data
        else if (
            this.injectionModel.getMediatorUtils().getConnectionUtil().getMethodInjection() == this.injectionModel.getMediatorMethodInjection().getRequest()
            && this.getRequest().isEmpty()
        ) {
            throw new InjectionFailureException("Incorrect Request format");
            
        } else if (
            this.injectionModel.getMediatorUtils().getConnectionUtil().getMethodInjection() == this.injectionModel.getMediatorMethodInjection().getHeader()
            && this.getHeader().isEmpty()
        ) {
            throw new InjectionFailureException("Incorrect Header format");
            
        }
    }
    
    // TODO Spock coverage
    public String getCharacterInsertion(boolean isParamByUser, SimpleEntry<String, String> parameter) {
        String characterInsertionByUser = "";
        
        // Parse query information: url=>everything before the sign '=',
        // start of query string=>everything after '='
        if (this.injectionModel.getMediatorUtils().getConnectionUtil().getMethodInjection() == this.injectionModel.getMediatorMethodInjection().getQuery()) {
            if (
                !isParamByUser
                && (
                    this.getQueryStringFromEntries().contains(InjectionModel.STAR)
                    || this.injectionModel.getMediatorUtils().getConnectionUtil().getUrlBase().contains(InjectionModel.STAR)
                )
            ) {
                if (parameter != null) {
                    parameter.setValue(InjectionModel.STAR);
                }
                return InjectionModel.STAR;
            } else if (parameter != null) {
                characterInsertionByUser = parameter.getValue();
                parameter.setValue(InjectionModel.STAR);
            }
            
        // Parse post information
        } else if (this.injectionModel.getMediatorUtils().getConnectionUtil().getMethodInjection() == this.injectionModel.getMediatorMethodInjection().getRequest()) {
            if (
                !isParamByUser
                && this.getRequestFromEntries().contains(InjectionModel.STAR)
            ) {
                if (parameter != null) {
                    parameter.setValue(InjectionModel.STAR);
                }
                return InjectionModel.STAR;
            } else if (parameter != null) {
                characterInsertionByUser = parameter.getValue();
                parameter.setValue(InjectionModel.STAR);
            }
            
        // Parse header information
        } else if (this.injectionModel.getMediatorUtils().getConnectionUtil().getMethodInjection() == this.injectionModel.getMediatorMethodInjection().getHeader()) {
            if (
                !isParamByUser
                && this.getHeaderFromEntries().contains(InjectionModel.STAR)
            ) {
                if (parameter != null) {
                    parameter.setValue(InjectionModel.STAR);
                }
                return InjectionModel.STAR;
            } else if (parameter != null) {
                characterInsertionByUser = parameter.getValue();
                parameter.setValue(InjectionModel.STAR);
            }
        }
        
        return characterInsertionByUser;
    }
    
    public String getQueryStringFromEntries() {
        return this.queryString
            .stream()
            .filter(Objects::nonNull)
            .map(e -> e.getKey() +"="+ e.getValue())
            .collect(Collectors.joining("&"));
    }

    public String getRequestFromEntries() {
        return this.request
            .stream()
            .filter(Objects::nonNull)
            .map(e -> e.getKey() +"="+ e.getValue())
            .collect(Collectors.joining("&"));
    }
    
    public String getHeaderFromEntries() {
        return this.header
            .stream()
            .filter(Objects::nonNull)
            .map(e -> e.getKey() +":"+ e.getValue())
            .collect(Collectors.joining("\\r\\n"));
    }

    public void initQueryString(String urlQuery) throws MalformedURLException {
        URL url = new URL(urlQuery);
        if ("".equals(urlQuery) || "".equals(url.getHost())) {
            throw new MalformedURLException("empty URL");
        }
        
        this.injectionModel.getMediatorUtils().getConnectionUtil().setUrlByUser(urlQuery);
        
        // Parse url and GET query string
        this.setQueryString(new ArrayList<SimpleEntry<String, String>>());
        Matcher regexSearch = Pattern.compile("(.*\\?)(.*)").matcher(urlQuery);
        if (regexSearch.find()) {
            this.injectionModel.getMediatorUtils().getConnectionUtil().setUrlBase(regexSearch.group(1));
            
            if (!"".equals(url.getQuery())) {
                this.setQueryString(
                    Pattern
                    .compile("&")
                    .splitAsStream(regexSearch.group(2))
                    .map(s -> Arrays.copyOf(s.split("="), 2))
                    .map(o -> new SimpleEntry<>(o[0], o[1] == null ? "" : o[1]))
                    .collect(Collectors.toList())
                );
            } else {
                this.queryString.clear();
            }
        } else {
            this.injectionModel.getMediatorUtils().getConnectionUtil().setUrlBase(urlQuery);
        }
    }

    public void initRequest(String request) {
        this.requestAsText = request;
        
        if (!"".equals(request)) {
            this.setRequest(
                Pattern
                .compile("&")
                .splitAsStream(request)
                .map(s -> Arrays.copyOf(s.split("="), 2))
                .map(o -> new SimpleEntry<>(o[0], o[1] == null ? "" : o[1]))
                .collect(Collectors.toList())
            );
        } else {
            this.request.clear();
        }
    }

    public void initHeader(String header) {
        if (!"".equals(header)) {
            this.setHeader(
                Pattern
                .compile("\\\\r\\\\n")
                .splitAsStream(header)
                .map(s -> Arrays.copyOf(s.split(":"), 2))
                .map(o -> new SimpleEntry<>(o[0], o[1] == null ? "" : o[1]))
                .collect(Collectors.toList())
            );
        } else {
            this.header.clear();
        }
    }

    public boolean isRequestSoap() {
        return this.requestAsText.trim().matches("^<\\?xml.*");
    }

    // Getters / setters
    
    public List<SimpleEntry<String, String>> getRequest() {
        return this.request;
    }

    public void setRequest(List<SimpleEntry<String, String>> request) {
        this.request = request;
    }

    public List<SimpleEntry<String, String>> getHeader() {
        return this.header;
    }

    public void setHeader(List<SimpleEntry<String, String>> header) {
        this.header = header;
    }
    
    public List<SimpleEntry<String, String>> getQueryString() {
        return this.queryString;
    }
    
    public void setQueryString(List<SimpleEntry<String, String>> queryString) {
        this.queryString = queryString;
    }

    public String getRawRequest() {
        return this.requestAsText;
    }

}
