package com.jsql.model.injection.strategy.blind;

import java.util.LinkedList;
import java.util.List;

import com.jsql.model.InjectionModel;
import com.jsql.model.injection.strategy.blind.patch.Diff;
import com.jsql.model.injection.strategy.blind.patch.DiffMatchPatch;

/**
 * 定义对服务器的调用HTTP，要求关联的url，字符位置和位。 操作码代表了TRUE页面和结果页面。
 */
public class CallableBlind extends AbstractCallableBoolean<CallableBlind> {
    
    /**
     * TRUE页面和当前页面之间差异的集合。
     */
    private LinkedList<Diff> opcodes = new LinkedList<>();
    
    private static final DiffMatchPatch DIFFMATCHPATCH = new DiffMatchPatch();

    /**
     * 构造方法 进行准备和盲目确认
     * @param inj
     * @param injectionBlind
     */
    InjectionBlind injectionBlind;
    public CallableBlind(String inj, InjectionModel injectionModel, InjectionBlind injectionBlind) {
        this.injectionModel = injectionModel;
        this.injectionBlind = injectionBlind;
        this.blindUrl = this.injectionModel.getMediatorVendor().getVendor().instance().sqlTestBlind(inj);
    }
    
    /**
     * 位测试的构造函数
     * @param inj
     * @param indexCharacter
     * @param bit
     * @param injectionModel
     */
    InjectionModel injectionModel;
    public CallableBlind(String inj, int indexCharacter, int bit, InjectionModel injectionModel, InjectionBlind injectionBlind) {
        this.injectionBlind = injectionBlind;
        this.injectionModel = injectionModel;
        this.blindUrl = this.injectionModel.getMediatorVendor().getVendor().instance().sqlBitTestBlind(inj, indexCharacter, bit);
        this.currentIndex = indexCharacter;
        this.currentBit = bit;
    }
    
    /**
     * 长度测试的构造函数。
     * @param inj
     * @param indexCharacter
     * @param isTestingLength
     * @param injectionModel
     * @param injectionBlind2
     */
    public CallableBlind(String inj, int indexCharacter, boolean isTestingLength, InjectionModel injectionModel, InjectionBlind injectionBlind) {
        this.injectionBlind = injectionBlind;
        this.injectionModel = injectionModel;
        this.blindUrl = this.injectionModel.getMediatorVendor().getVendor().instance().sqlLengthTestBlind(inj, indexCharacter);
        this.isTestingLength = isTestingLength;
    }

    /**
     * 检查结果页是否表示SQL查询为true，在每个FALSE SQL查询的页面中确认结果页面中也未定义任何内容
      * @return如果当前SQL查询为true，则为true
     */
    @Override
    public boolean isTrue() {
        for (Diff falseDiff: this.injectionBlind.getConstantFalseMark()) {
            // 操作码被初始化为空的 new LinkedList <>（）
            if (this.opcodes.contains(falseDiff)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 处理URL HTTP调用，使用模型中的函数inject（）。
     * 建立TRUE与当前页面之间的差异集合列表。
     * @返回功能盲可调用
     */
    @Override
    public CallableBlind call() throws Exception {
        String ctnt = this.injectionBlind.callUrl(this.blindUrl);
        this.opcodes = DIFFMATCHPATCH.diffMain(this.injectionBlind.getBlankTrueMark(), ctnt, true);
        DIFFMATCHPATCH.diffCleanupEfficiency(this.opcodes);
        return this;
    }
    
    public List<Diff> getOpcodes() {
        return this.opcodes;
    }
    
}
