package com.jsql.model.accessible;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.jsql.model.InjectionModel;
import com.jsql.model.exception.IgnoreMessageException;
import com.jsql.model.exception.InjectionFailureException;
import com.jsql.model.exception.StoppedByUserSlidingException;
import com.jsql.model.suspendable.SuspendableGetRows;

/**
 * 通过SQL注入读取文件源的线程单元，用户可以中断该过程并获得文件内容的部分结果。
 */
public class CallableFile implements Callable<CallableFile> {
    

    private static final Logger LOGGER = Logger.getRootLogger();
    
    /**
     * 要读取的文件的路径。
     */
    private String pathFile;

    /**
     * 源文件
     */
    private String sourceFile = "";
    
    /**
     * 通过注入读取文件行的可挂起任务
     */
    private SuspendableGetRows suspendableReadFile;

    /**
     * 创建可调用以读取文件。
     * @param pathFile
     */
    public CallableFile(String pathFile, InjectionModel injectionModel) {
        this.pathFile = pathFile;
        this.injectionModel= injectionModel;
        this.suspendableReadFile = new SuspendableGetRows(injectionModel);
    }
    InjectionModel injectionModel;
    
    /**
     * 使用SQL注入读取服务器上的文件，如果用户中断该过程，则会得到部分结果。
     */
    @Override
    public CallableFile call() throws Exception {
        String[] sourcePage = {""};

        String resultToParse = "";
        try {
            resultToParse = this.suspendableReadFile.run(
                this.injectionModel.getMediatorVendor().getVendor().instance().sqlFileRead(this.pathFile),
                sourcePage,
                false,
                1,
                null
            );
        } catch (InjectionFailureException e) {
            // Usually thrown if File does not exist
            
            // Ignore
            IgnoreMessageException exceptionIgnored = new IgnoreMessageException(e);
            LOGGER.trace(exceptionIgnored, exceptionIgnored);
        } catch (StoppedByUserSlidingException e) {
            // 获取部分来源
            if (!"".equals(e.getSlidingWindowAllRows())) {
                resultToParse = e.getSlidingWindowAllRows();
            } else if (!"".equals(e.getSlidingWindowCurrentRows())) {
                resultToParse = e.getSlidingWindowCurrentRows();
            }
            
            // Ignore
            IgnoreMessageException exceptionIgnored = new IgnoreMessageException(e);
            LOGGER.trace(exceptionIgnored, exceptionIgnored);
        }
        this.sourceFile = resultToParse;
        
        return this;
    }
    
    // Getters and setters
    
    public String getPathFile() {
        return this.pathFile;
    }

    public String getSourceFile() {
        return this.sourceFile;
    }

    public SuspendableGetRows getSuspendableReadFile() {
        return this.suspendableReadFile;
    }
    
}