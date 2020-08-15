package com.jsql.view.interaction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import com.jsql.model.bean.util.Request;
import com.jsql.model.exception.IgnoreMessageException;

public class ObserverInteraction implements Observer {

    private static final Logger LOGGER = Logger.getRootLogger();
    
    private String packageInteraction;
    
    public ObserverInteraction(String packageInteraction) {
        this.packageInteraction = packageInteraction;
    }
        
    /**
      *	观察者模式--从模型接收更新命令：
      *	  -使用Request消息获取Interaction类
      *	  -将参数传递给该类。
     */
    @Override
    public void update(Observable model, Object newInteraction) {
        Request interaction = (Request) newInteraction;

        try {
            Class<?> cl = Class.forName(this.packageInteraction +"."+ interaction.getMessage());
            Class<?>[] types = new Class[]{Object[].class};
            Constructor<?> ct = cl.getConstructor(types);

            InteractionCommand o2 = (InteractionCommand) ct.newInstance(new Object[]{interaction.getParameters()});
            o2.execute();
        } catch (ClassNotFoundException e) {
            // 忽略没用的信息
            IgnoreMessageException ignore = new IgnoreMessageException(e);
            LOGGER.trace(ignore, ignore);
        } catch (
            InstantiationException | IllegalAccessException | NoSuchMethodException |
            SecurityException | IllegalArgumentException | InvocationTargetException e
        ) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
}
