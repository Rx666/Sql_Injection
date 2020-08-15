package com.jsql.model.injection.strategy.blind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.jsql.model.InjectionModel;
import com.jsql.model.exception.StoppedByUserSlidingException;
import com.jsql.model.injection.strategy.blind.patch.Diff;
import com.jsql.model.suspendable.callable.ThreadFactoryCallable;

/**
 * 使用线程异步的盲注攻击类
 */
public class InjectionBlind extends AbstractInjectionBoolean<CallableBlind> {
    
    private static final Logger LOGGER = Logger.getRootLogger();

    /**
     * TRUE网页的源代码（通常为？id = 1）
     */
    private String blankTrueMark;

    /**
     * 差异列表-FALSE查询中发现的字符串TRUE页面（又名操作码）。 每个FALSE页面应包含至少一个相同的字符串，不应全部出现TRUE查询。
     */
    private List<Diff> constantFalseMark = new ArrayList<>();

    /**
     *创建盲目的攻击初始化。如果每个错误测试均未在真实标记中，并且每个真实测试均已在,真实测试，然后确认盲注攻击。
     */
    public InjectionBlind(InjectionModel injectionModel) {
        super(injectionModel);
        
        // No blind
        if (this.falseTest.length == 0) {
            return;
        }
        
        // Call the SQL request which must be TRUE (usually ?id=1)
        this.blankTrueMark = this.callUrl("");

        // Check if the user wants to stop the preparation
        if (this.injectionModel.isStoppedByUser()) {
            return;
        }

        /*
         *  并行调用FALSE语句，使用模型中的inject（）
         */
        ExecutorService executorTagFalse = Executors.newCachedThreadPool(new ThreadFactoryCallable("CallableGetBlindTagFalse"));
        Collection<CallableBlind> listCallableTagFalse = new ArrayList<>();
        for (String urlTest: this.falseTest) {
            listCallableTagFalse.add(new CallableBlind(urlTest, injectionModel, this));
        }
        
        /*
       	 *从FALSE语句的结果中删除垃圾，仅保留在每个FALSE页中找到的操作码。允许用户停止循环
         */
        try {
            // Begin the url requests
            List<Future<CallableBlind>> listTagFalse = null;
            listTagFalse = executorTagFalse.invokeAll(listCallableTagFalse);
            executorTagFalse.shutdown();
            
            this.constantFalseMark = listTagFalse.get(0).get().getOpcodes();
            for (Future<CallableBlind> falseMark: listTagFalse) {
                if (this.injectionModel.isStoppedByUser()) {
                    return;
                }
                this.constantFalseMark.retainAll(falseMark.get().getOpcodes());
            }
        } catch (ExecutionException e) {
            LOGGER.error("Searching fails for Blind False tags", e);
        } catch (InterruptedException e) {
            LOGGER.error("Interruption while searching for Blind False tags", e);
            Thread.currentThread().interrupt();
        }

        if (this.injectionModel.isStoppedByUser()) {
            return;
        }

        /*
         *  并行调用TRUE语句，它将使用模型中的inject（）。
         */
        ExecutorService executorTagTrue = Executors.newCachedThreadPool(new ThreadFactoryCallable("CallableGetBlindTagTrue"));
        Collection<CallableBlind> listCallableTagTrue = new ArrayList<>();
        for (String urlTest: this.trueTest) {
            listCallableTagTrue.add(new CallableBlind(urlTest, injectionModel, this));
        }
        
        // Begin the url requests
        List<Future<CallableBlind>> listTagTrue = null;
        try {
            listTagTrue = executorTagTrue.invokeAll(listCallableTagTrue);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        executorTagTrue.shutdown();

        /*
         * 删除FALSE操作码中的TRUE操作码，因为重要的FALSE语句不应包含任何TRUE操作码,允许用户停止循环。
         */
        try {
            for (Future<CallableBlind> trueTag: listTagTrue) {
                if (this.injectionModel.isStoppedByUser()) {
                    return;
                }
                this.constantFalseMark.removeAll(trueTag.get().getOpcodes());
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public CallableBlind getCallable(String string, int indexCharacter, boolean isTestingLength) {
        return new CallableBlind(string, indexCharacter, isTestingLength, this.injectionModel, this);
    }

    @Override
    public CallableBlind getCallable(String string, int indexCharacter, int bit) {
        return new CallableBlind(string, indexCharacter, bit, this.injectionModel, this);
    }

    @Override
    public boolean isInjectable() throws StoppedByUserSlidingException {
        if (this.injectionModel.isStoppedByUser()) {
            throw new StoppedByUserSlidingException();
        }
        
        CallableBlind blindTest = new CallableBlind(this.injectionModel.getMediatorVendor().getVendor().instance().sqlTestBlindFirst(), this.injectionModel, this);
        try {
            blindTest.call();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return blindTest.isTrue() && !this.constantFalseMark.isEmpty();
    }

    @Override
    public String getInfoMessage() {
        return
            "Blind strategy: a request is true if the diff between "
            + "a correct page (e.g existing id) and current page "
            + "is not as the following: "
            + this.constantFalseMark
        ;
    }

    /**
     * Get source code of the TRUE web page.
     * @return Source code in HTML
     */
    public String getBlankTrueMark() {
        return this.blankTrueMark;
    }
    
    /**
     *  Get False Marks.
     *  @return False marks
     */
    public List<Diff> getConstantFalseMark() {
        return this.constantFalseMark;
    }
    
}
