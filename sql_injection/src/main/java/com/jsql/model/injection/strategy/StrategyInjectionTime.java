package com.jsql.model.injection.strategy;

import org.apache.log4j.Logger;

import com.jsql.i18n.I18n;
import com.jsql.model.InjectionModel;
import com.jsql.model.bean.SubUrl;
import com.jsql.model.bean.util.Interaction;
import com.jsql.model.bean.util.Request;
import com.jsql.model.exception.StoppedByUserSlidingException;
import com.jsql.model.injection.strategy.blind.InjectionTime;
import com.jsql.model.suspendable.AbstractSuspendable;

/**
 * 基于时间的注入
 */
public class StrategyInjectionTime extends AbstractStrategy {
    
    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = Logger.getRootLogger();

    /**
     * Injection method using time attack.
     */
    private InjectionTime timeInjection;
    
    public StrategyInjectionTime(InjectionModel injectionModel) {
        super(injectionModel);
    }

    @Override
    public void checkApplicability() throws StoppedByUserSlidingException {
        
        if (this.injectionModel.getMediatorVendor().getVendor().instance().sqlTestBlindFirst() == null) {
            LOGGER.info("No Time strategy known for "+ this.injectionModel.getMediatorVendor().getVendor());
        } else {
            LOGGER.trace(I18n.valueByKey("LOG_CHECKING_STRATEGY") +" Time...");
            
            this.timeInjection = new InjectionTime(this.injectionModel);
            
            this.isApplicable = this.timeInjection.isInjectable();
            
            if (this.isApplicable) {
                LOGGER.debug(I18n.valueByKey("LOG_VULNERABLE") +" Time injection");
                this.allow();
                
                //更新数据库数据
                Request updateDB = new Request();
                updateDB.setMessage(Interaction.UPDATE_INJECTION_STATUS_DB);
                SubUrl subUrl = new SubUrl();
                subUrl.setCrawlUrl(this.injectionModel.getMediatorUtils().getConnectionUtil().getUrlByUser());
                subUrl.setTimeInjection("1");
                subUrl.setIsInjection("1");
                updateDB.setParameters("time",subUrl);
                this.injectionModel.sendToViews(updateDB);
                
                Request requestMessageBinary = new Request();
                requestMessageBinary.setMessage(Interaction.MESSAGE_BINARY);
                requestMessageBinary.setParameters(this.timeInjection.getInfoMessage());
                this.injectionModel.sendToViews(requestMessageBinary);
            } else {
                this.unallow();
            }
        }
        
    }
    
    @Override
    public void allow(int... i) {
        this.markVulnerability(Interaction.MARK_TIME_VULNERABLE);
    }

    @Override
    public void unallow(int... i) {
        this.markVulnerability(Interaction.MARK_TIME_INVULNERABLE);
    }

    @Override
    public String inject(String sqlQuery, String startPosition, AbstractSuspendable<String> stoppable) throws StoppedByUserSlidingException {
        return this.timeInjection.inject(
            this.injectionModel.getMediatorVendor().getVendor().instance().sqlTime(sqlQuery, startPosition),
            stoppable
        );
    }

    @Override
    public void activateStrategy() {
        LOGGER.info(I18n.valueByKey("LOG_USING_STRATEGY") +" ["+ this.getName() +"]");
        this.injectionModel.getMediatorStrategy().setStrategy(this.injectionModel.getMediatorStrategy().getTime());
        
        Request requestMarkTimeStrategy = new Request();
        requestMarkTimeStrategy.setMessage(Interaction.MARK_TIME_STRATEGY);
        this.injectionModel.sendToViews(requestMarkTimeStrategy);
    }
    
    @Override
    public String getPerformanceLength() {
        return "65565";
    }
    
    @Override
    public String getName() {
        return "Time";
    }
    
}
