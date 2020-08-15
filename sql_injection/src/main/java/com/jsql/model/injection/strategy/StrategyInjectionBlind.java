package com.jsql.model.injection.strategy;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.jsql.i18n.I18n;
import com.jsql.model.InjectionModel;
import com.jsql.model.bean.SubUrl;
import com.jsql.model.bean.util.Interaction;
import com.jsql.model.bean.util.Request;
import com.jsql.model.exception.StoppedByUserSlidingException;
import com.jsql.model.injection.strategy.blind.InjectionBlind;
import com.jsql.model.suspendable.AbstractSuspendable;

/**
 * 盲注策略
 */
@Component
public class StrategyInjectionBlind extends AbstractStrategy {
    
    private static final Logger LOGGER = Logger.getRootLogger();
    
    /**
     * 盲注对象
     */
    private InjectionBlind blind;
    
    public StrategyInjectionBlind(InjectionModel injectionModel) {
        super(injectionModel);
    }

    @Override
    public void checkApplicability() throws StoppedByUserSlidingException {
        
        if (this.injectionModel.getMediatorVendor().getVendor().instance().sqlTestBlindFirst() == null) {
            LOGGER.info("无盲注策略用于 "+ this.injectionModel.getMediatorVendor().getVendor());
        } else {
            LOGGER.trace("检查策略中" +" Blind...");
            
            this.blind = new InjectionBlind(this.injectionModel);
            this.isApplicable = this.blind.isInjectable();
           
            if (this.isApplicable) {
                LOGGER.debug("可能存在" +" Blind injection");
                //更新数据库数据
                Request updateDB = new Request();
                updateDB.setMessage(Interaction.UPDATE_INJECTION_STATUS_DB);
                SubUrl subUrl = new SubUrl();
                subUrl.setCrawlUrl(this.injectionModel.getMediatorUtils().getConnectionUtil().getUrlByUser());
                subUrl.setBlindInjection("1");
                subUrl.setIsInjection("1");
                updateDB.setParameters("blind",subUrl);
                this.injectionModel.sendToViews(updateDB);
                
                this.allow();
                
                
                Request requestMessageBinary = new Request();
                requestMessageBinary.setMessage(Interaction.MESSAGE_BINARY);
                requestMessageBinary.setParameters(this.blind.getInfoMessage());
                this.injectionModel.sendToViews(requestMessageBinary);
            } else {
                this.unallow();
            }
        }
        
    }

    @Override
    public void allow(int... i) {
        this.markVulnerability(Interaction.MARK_BLIND_VULNERABLE);
    }

    @Override
    public void unallow(int... i) {
        this.markVulnerability(Interaction.MARK_BLIND_INVULNERABLE);
    }

    @Override
    public String inject(String sqlQuery, String startPosition, AbstractSuspendable<String> stoppable) throws StoppedByUserSlidingException {
        return this.blind.inject(
            this.injectionModel.getMediatorVendor().getVendor().instance().sqlBlind(sqlQuery, startPosition),
            stoppable
        );
    }

    @Override
    public void activateStrategy() {
        LOGGER.info(I18n.valueByKey("LOG_USING_STRATEGY") +" ["+ this.getName() +"]");
        this.injectionModel.getMediatorStrategy().setStrategy(this.injectionModel.getMediatorStrategy().getBlind());
        
        Request requestMarkBlindStrategy = new Request();
        requestMarkBlindStrategy.setMessage(Interaction.MARK_BLIND_STRATEGY);
        this.injectionModel.sendToViews(requestMarkBlindStrategy);
    }
    
    @Override
    public String getPerformanceLength() {
        return "65565";
    }
    
    @Override
    public String getName() {
        return "Blind";
    }
    
}
