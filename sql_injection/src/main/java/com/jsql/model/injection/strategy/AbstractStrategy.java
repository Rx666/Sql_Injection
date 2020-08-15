package com.jsql.model.injection.strategy;

import java.util.EnumMap;
import java.util.Map;

import com.jsql.model.InjectionModel;
import com.jsql.model.bean.util.Header;
import com.jsql.model.bean.util.Interaction;
import com.jsql.model.bean.util.Request;
import com.jsql.model.exception.InjectionFailureException;
import com.jsql.model.exception.JSqlException;
import com.jsql.model.exception.StoppedByUserSlidingException;
import com.jsql.model.suspendable.AbstractSuspendable;

/**
 * 定义一种策略，以使用诸如Error和Time之类的方法注入SQL。
 */
public abstract class AbstractStrategy {
    
    /**
     * i.e, 2 in "[..]union select 1,2,[..]", if 2 is found in HTML body.
     */
    protected String visibleIndex;
    
    protected InjectionModel injectionModel;
    
    public AbstractStrategy(InjectionModel injectionModel) {
        this.injectionModel = injectionModel;
    }
    
    /**
     * 如果可以注入，则为true，否则为false。
     */
    protected boolean isApplicable = false;

    /**
     *	返回 此策略可用于SQL是否可以注入
     *  @return如果可以使用策略，则为True，否则为false。
     */
    public boolean isApplicable() {
        return this.isApplicable;
    }

    /**
     * 测试此策略是否可以用于注入SQL。
     * @return
     * @throws InjectionFailureException
     * @throws StoppedByUserSlidingException
     */
    public abstract void checkApplicability() throws JSqlException;
    
    /**
     * 通知视图可以使用此策略。
     */
    protected abstract void allow(int... i);
    
    /**
     * 通知视图该策略无法使用。
     */
    protected abstract void unallow(int... i);
    
    public void markVulnerability(Interaction message, int... i) {
        Request request = new Request();
        request.setMessage(message);
        
        Map<Header, Object> msgHeader = new EnumMap<>(Header.class);
        msgHeader.put(Header.URL, this.injectionModel.getMediatorUtils().getConnectionUtil().getUrlByUser());
        
        if (i != null && i.length > 0) {
            msgHeader.put(Header.SOURCE, i[0]);
            msgHeader.put(Header.INJECTION_MODEL, this.injectionModel);
        }

        request.setParameters(msgHeader);
        this.injectionModel.sendToViews(request);
    }
    
    /**
     * 开始策略工作
     * @return Source code
     */
    public abstract String inject(String sqlQuery, String startPosition, AbstractSuspendable<String> stoppable) throws StoppedByUserSlidingException;
    
    /**
     * 将模型的策略更改为当前策略。
     */
    public abstract void activateStrategy();
    
    /**
     * 获取可以从策略中获取的字符数。
     */
    public abstract String getPerformanceLength();
    
    /**
     * 获取注入策略名称。
     */
    public abstract String getName();
    
    @Override
    public String toString() {
        return this.getName();
    }
    
    // TODO strategy Error
    public Integer getIndexMethodError() {
        return null;
    }
    
    public String getVisibleIndex() {
        return this.visibleIndex;
    }

    public void setVisibleIndex(String visibleIndex) {
        this.visibleIndex = visibleIndex;
    }
    
}
