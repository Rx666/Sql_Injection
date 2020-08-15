package com.jsql.model.suspendable;

import static com.jsql.model.accessible.DataAccess.ENCLOSE_VALUE_RGX;
import static com.jsql.model.accessible.DataAccess.LEAD;
import static com.jsql.model.accessible.DataAccess.MODE;
import static com.jsql.model.accessible.DataAccess.SEPARATOR_CELL_RGX;
import static com.jsql.model.accessible.DataAccess.SEPARATOR_QTE_RGX;
import static com.jsql.model.accessible.DataAccess.TRAIL_RGX;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.jsql.model.InjectionModel;
import com.jsql.model.bean.database.AbstractElementDatabase;
import com.jsql.model.bean.database.Table;
import com.jsql.model.bean.util.Interaction;
import com.jsql.model.bean.util.Request;
import com.jsql.model.exception.InjectionFailureException;
import com.jsql.model.exception.JSqlException;
import com.jsql.model.exception.LoopDetectedSlidingException;
import com.jsql.model.exception.StoppedByUserSlidingException;
import com.jsql.model.injection.strategy.AbstractStrategy;
import com.jsql.util.StringUtil;

/**
 * 	从SQL请求中获取所有数据（数据将经常被切断，所以需要访问所有数据）
  *	需要遵循以下格式良：
  * => hh [0-9A-F] * jj [0-9A-F] * c？hhgghh [0-9A-F] * jj [0-9A-F] * c？hhg ... hi
  *	我们必须检测运行是否被中断，以及在何处被中断，基本上我们将在虚拟二位数组中移动位置，并使用LIMIT和MID移动光标; LIMIT跳过整行（如果结果包含1个或多个完整行则很有用）；
  *	 和MID跳过一行中的字符（如果结果包含少于1行，则很有用），该过程可以被用户中断（停止/暂停）。
 */
public class SuspendableGetRows extends AbstractSuspendable<String> {
    
    public SuspendableGetRows(InjectionModel injectionModel) {
        super(injectionModel);
    }

    @Override
    public String run(Object... args) throws JSqlException {
        String initialSQLQuery = (String) args[0];
        String[] sourcePage = (String[]) args[1];
        boolean isUsingLimit = (Boolean) args[2];
        int numberToFind = (Integer) args[3];
        AbstractElementDatabase searchName = (AbstractElementDatabase) args[4];
        this.injectionModel.getMediatorUtils().getThreadUtil().put(searchName, this);

        String sqlQuery = initialSQLQuery.replaceAll("\\{limit\\}", this.injectionModel.getMediatorVendor().getVendor().instance().sqlLimit(0));

        AbstractStrategy strategy;
        // Fix #14417
        // TODO Optional
        if (this.injectionModel.getMediatorStrategy().getStrategy() != null) {
            strategy = this.injectionModel.getMediatorStrategy().getStrategy();
        } else {
            return "";
        }
        
        /*
         * As we know the expected number of rows (numberToFind), then it stops injection if all rows are found,
         * keep track of rows we have reached (limitSQLResult) and use these to skip entire rows,
         * keep track of characters we have reached (startPosition) and use these to skip characters,
         */
        StringBuilder slidingWindowAllRows = new StringBuilder();
        String partOldRow = "";
        StringBuilder slidingWindowCurrentRow = new StringBuilder();
        int sqlLimit = 0;
        int charPositionInCurrentRow = 1;
        int infiniteLoop = 0;
        
        while (true) {

            if (this.isSuspended()) {
                throw new StoppedByUserSlidingException(
                    slidingWindowAllRows.toString(),
                    slidingWindowCurrentRow.toString()
                );
            } else if (strategy == null) {
                // Fix #1905 : NullPointerException on injectionStrategy.inject()
                throw new InjectionFailureException("Undefined startegy");
            }
            
            sourcePage[0] = strategy.inject(sqlQuery, Integer.toString(charPositionInCurrentRow), this);
            
            /**
             * After ${LEAD} tag, gets characters between 1 and maxPerf
             * performanceQuery() gets 65536 characters or less
             * ${LEAD}blahblah1337      ] : end or limit+1
             * ${LEAD}blahblah      blah] : continue substr()
             */
            // Parse all the data we have retrieved
            Matcher regexAtLeastOneRow;
            try {
                regexAtLeastOneRow =
                    Pattern
                        .compile("(?s)"+ LEAD +"(?i)(.{1,"+ strategy.getPerformanceLength() +"})")
                        .matcher(sourcePage[0]);
            } catch (PatternSyntaxException e) {
                // Fix #35382 : PatternSyntaxException null on SQLi(.{1,null})
                throw new InjectionFailureException("Row parsing failed using capacity", e);
            }
            
            // TODO: prevent to find the last line directly: MODE + LEAD + .* + TRAIL_RGX
            // It creates extra query which can be endless if not nullified
            Matcher regexEndOfLine =
                Pattern
                    .compile("(?s)"+ LEAD +"(?i)"+ TRAIL_RGX)
                    .matcher(sourcePage[0]);
            
            if (regexEndOfLine.find() && isUsingLimit && !"".equals(slidingWindowAllRows.toString())) {
                // Update the view only if there are value to find, and if it's not the root (empty tree)
                if (numberToFind > 0 && searchName != null) {
                    Request request = new Request();
                    request.setMessage(Interaction.UPDATE_PROGRESS);
                    request.setParameters(searchName, numberToFind);
                    this.injectionModel.sendToViews(request);
                }
                break;
            }
            
            /*
             * Ending condition:
             * One row could be very long, longer than the database can provide
             * TODO Need verification
             */
            if (!regexAtLeastOneRow.find() && isUsingLimit && !"".equals(slidingWindowAllRows.toString())) {
                // Update the view only if there are value to find, and if it's not the root (empty tree)
                if (numberToFind > 0 && searchName != null) {
                    Request request = new Request();
                    request.setMessage(Interaction.UPDATE_PROGRESS);
                    request.setParameters(searchName, numberToFind);
                    this.injectionModel.sendToViews(request);
                }
                break;
            }

            /*
             * Add the result to the data already found.
             */
            // Fix #40947: OutOfMemoryError on append()
            try {
                if (partOldRow.equals(regexAtLeastOneRow.group(1))) {
                    infiniteLoop++;
                    if (infiniteLoop >= 20) {
                        throw new LoopDetectedSlidingException(
                            slidingWindowAllRows.toString(),
                            slidingWindowCurrentRow.toString()
                        );
                    }
                }
                partOldRow = regexAtLeastOneRow.group(1);
                
                slidingWindowCurrentRow.append(regexAtLeastOneRow.group(1));

                Request request = new Request();
                request.setMessage(Interaction.MESSAGE_CHUNK);
                request.setParameters(
                    Pattern
                        .compile(MODE + TRAIL_RGX +".*")
                        .matcher(regexAtLeastOneRow.group(1))
                        .replaceAll("")
                        .replaceAll("\\n", "\\\\\\n").replaceAll("\\r", "\\\\\\r").replaceAll("\\t", "\\\\\\t")
                );
                this.injectionModel.sendToViews(request);
            } catch (IllegalStateException | OutOfMemoryError e) {
                // Premature end of results
                // if it's not the root (empty tree)
                if (searchName != null) {
                    Request request = new Request();
                    request.setMessage(Interaction.END_PROGRESS);
                    request.setParameters(searchName);
                    this.injectionModel.sendToViews(request);
                }

                StringBuilder messageError = new StringBuilder("Fetching fails: no data to parse");
                
                if (searchName != null) {
                    messageError.append(" for "+ StringUtil.detectUtf8(searchName.toString()));
                }
                
                if (searchName instanceof Table && searchName.getChildCount() > 0) {
                    messageError.append(", if possible retry with one column selected only");
                }
                
                throw new InjectionFailureException(messageError.toString(), e);
            }

            /*
             * Check how many rows we have collected from the beginning of that chunk
             */
            regexAtLeastOneRow =
                Pattern
                    .compile(
                        MODE
                        +"("
                            + ENCLOSE_VALUE_RGX
                            + "([^\\x01-\\x09\\x0B-\\x0C\\x0E-\\x1F]*?)"
                            + SEPARATOR_QTE_RGX
                            + "([^\\x01-\\x09\\x0B-\\x0C\\x0E-\\x1F]*?)(\\x08)?"
                            + ENCLOSE_VALUE_RGX
                        + ")"
                    )
                    .matcher(slidingWindowCurrentRow);
            int nbCompleteLine = 0;
            while (regexAtLeastOneRow.find()) {
                nbCompleteLine++;
            }

            /*
             * Inform the view about the progression
             */
            if (isUsingLimit && numberToFind > 0 && searchName != null) {
                Request request = new Request();
                request.setMessage(Interaction.UPDATE_PROGRESS);
                request.setParameters(searchName, sqlLimit + nbCompleteLine);
                this.injectionModel.sendToViews(request);
            }

            /*
             * We have properly reached the i at the end of the query: iLQS
             * => hhxxxxxxxxjj00hhgghh...hiLQS
             */
            /* Design Pattern: State? */
            if (nbCompleteLine>0 || slidingWindowCurrentRow.toString().matches("(?s).*"+ TRAIL_RGX +".*")) {
                /*
                 * Remove everything after our result
                 * => hhxxxxxxxxjj00hhgghh...h |-> iLQSjunk
                 */
                String currentRow = slidingWindowCurrentRow.toString();
                slidingWindowCurrentRow.setLength(0);
                slidingWindowCurrentRow.append(
                    Pattern
                        .compile(MODE + TRAIL_RGX +".*")
                        .matcher(currentRow)
                        .replaceAll("")
                );
                slidingWindowAllRows.append(slidingWindowCurrentRow.toString());
                if (isUsingLimit) {
                    /*
                     * Remove everything not properly attached to the last row:
                     * 1. very start of a new row: XXXXXhhg[ghh]$
                     * 2. very end of the last row: XXXXX[jj00]$
                     */
                    String allRowsLimit = slidingWindowAllRows.toString();
                    slidingWindowAllRows.setLength(0);
                    slidingWindowAllRows.append(
                        Pattern
                            .compile(
                                MODE +"("
                                    + SEPARATOR_CELL_RGX + ENCLOSE_VALUE_RGX
                                    +"|"
                                    + SEPARATOR_QTE_RGX +"\\d*"
                                +")$"
                            )
                            .matcher(allRowsLimit)
                            .replaceAll("")
                    );
                    String currentRowLimit = slidingWindowCurrentRow.toString();
                    slidingWindowCurrentRow.setLength(0);
                    slidingWindowCurrentRow.append(
                        Pattern
                            .compile(
                                MODE +"("
                                    + SEPARATOR_CELL_RGX + ENCLOSE_VALUE_RGX
                                    +"|"
                                    + SEPARATOR_QTE_RGX +"\\d*"
                                +")$"
                            )
                            .matcher(currentRowLimit)
                            .replaceAll("")
                    );

                    /*
                     * Check either if there is more than 1 row and if there is less than 1 complete row
                     */
                    regexAtLeastOneRow =
                        Pattern
                            .compile(
                                MODE
                                + "[^\\x01-\\x09\\x0B-\\x0C\\x0E-\\x1F]"
                                + ENCLOSE_VALUE_RGX
                                + SEPARATOR_CELL_RGX
                                + ENCLOSE_VALUE_RGX
                                + "[^\\x01-\\x09\\x0B-\\x0C\\x0E-\\x1F]+?$"
                            )
                            .matcher(slidingWindowCurrentRow);
                    Matcher regexRowIncomplete =
                        Pattern
                            .compile(
                                MODE
                                + ENCLOSE_VALUE_RGX
                                + "[^\\x01-\\x03\\x05-\\x09\\x0B-\\x0C\\x0E-\\x1F]+?$"
                            )
                            .matcher(slidingWindowCurrentRow);

                    /*
                     * If there is more than 1 row, delete the last incomplete one in order to restart properly from it at the next loop,
                     * else if there is 1 row but incomplete, mark it as cut with the letter c
                     */
                    if (regexAtLeastOneRow.find()) {
                        String allLine = slidingWindowAllRows.toString();
                        slidingWindowAllRows.setLength(0);
                        slidingWindowAllRows.append(
                            Pattern
                                .compile(
                                    MODE
                                    + ENCLOSE_VALUE_RGX
                                    + "[^\\x01-\\x09\\x0B-\\x0C\\x0E-\\x1F]+?$"
                                )
                                .matcher(allLine)
                                .replaceAll("")
                        );
                    } else if (regexRowIncomplete.find()) {
                        slidingWindowAllRows.append(StringUtil.hexstr("05") + "1" + StringUtil.hexstr("0804"));
                    }

                    /*
                     * Check how many rows we have collected from the very beginning of the query,
                     * then skip every rows we have already found via LIMIT
                     */
                    regexAtLeastOneRow =
                        /*
                         * Regex \\x{08}? not supported on Kali
                         * => \\x08? seems ok though
                         */
                        Pattern
                            .compile(
                                MODE +"("
                                    + ENCLOSE_VALUE_RGX
                                    + "[^\\x01-\\x09\\x0B-\\x0C\\x0E-\\x1F]*?"
                                    + SEPARATOR_QTE_RGX
                                    + "[^\\x01-\\x09\\x0B-\\x0C\\x0E-\\x1F]*?\\x08?"
                                    + ENCLOSE_VALUE_RGX
                                +")"
                            )
                            .matcher(slidingWindowAllRows);

                    nbCompleteLine = 0;
                    while (regexAtLeastOneRow.find()) {
                        nbCompleteLine++;
                    }
                    sqlLimit = nbCompleteLine;

                    // Inform the view about the progression
                    if (numberToFind > 0 && searchName != null) {
                        Request request = new Request();
                        request.setMessage(Interaction.UPDATE_PROGRESS);
                        request.setParameters(searchName, sqlLimit);
                        this.injectionModel.sendToViews(request);
                    }

                    /*
                     * Ending condition: every expected rows have been retrieved.
                     * Inform the view about the progression
                     */
                    if (sqlLimit == numberToFind) {
                        if (numberToFind > 0 && searchName != null) {
                            Request request = new Request();
                            request.setMessage(Interaction.UPDATE_PROGRESS);
                            request.setParameters(searchName, numberToFind);
                            this.injectionModel.sendToViews(request);
                        }
                        break;
                    }

                    /*
                     *  Add the LIMIT statement to the next SQL query and reset variables.
                     *  Put the character cursor to the beginning of the line, and reset the result of the current query
                     */
                    sqlQuery =
                        Pattern
                            .compile(MODE +"\\{limit\\}")
                            .matcher(initialSQLQuery)
                            .replaceAll(this.injectionModel.getMediatorVendor().getVendor().instance().sqlLimit(sqlLimit));

                    slidingWindowCurrentRow.setLength(0);
                } else {
                    // Inform the view about the progression
                    if (numberToFind > 0 && searchName != null) {
                        Request request = new Request();
                        request.setMessage(Interaction.UPDATE_PROGRESS);
                        request.setParameters(searchName, numberToFind);
                        this.injectionModel.sendToViews(request);
                    }
                    break;
                }
                
            }

            charPositionInCurrentRow = slidingWindowCurrentRow.length() + 1;
        }
        
        this.injectionModel.getMediatorUtils().getThreadUtil().remove(searchName);

        return slidingWindowAllRows.toString();
    }
    
}

