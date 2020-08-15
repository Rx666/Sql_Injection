package com.jsql.model.accessible;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.jsql.i18n.I18n;
import com.jsql.model.InjectionModel;
import com.jsql.model.bean.database.AbstractElementDatabase;
import com.jsql.model.bean.database.Column;
import com.jsql.model.bean.database.Database;
import com.jsql.model.bean.database.Table;
import com.jsql.model.bean.util.Interaction;
import com.jsql.model.bean.util.Request;
import com.jsql.model.exception.IgnoreMessageException;
import com.jsql.model.exception.InjectionFailureException;
import com.jsql.model.exception.JSqlException;
import com.jsql.model.exception.SlidingException;
import com.jsql.model.suspendable.SuspendableGetRows;

/**
 * 数据库资源对象，用于读取数据库，表，列和值的名称,使用最适合的注入策略。
 */
public class DataAccess {
    
    private static final Logger LOGGER = Logger.getRootLogger();
    
    /**
     * SQL字符标记注入结果的结尾。遇到此结构时，进程停止：SQLix01x03x03x07
     */
    // TODO idem XML
    public static final String TRAIL_SQL = "%01%03%03%07";
    public static final String TRAIL_HEX = "0x01030307";
    public static final String TRAIL = "iLQS";
    public static final String TRAIL_IN_SHELL = "${JSQL.TRAIL}";
    
    /**
     * 正则表达式字符标记注入结果的结尾。遇到此结构时，进程停止：SQLix01x03x03x07
     */
    public static final String TRAIL_RGX = "\\x01\\x03\\x03\\x07";
    
    public static final String SEPARATOR_FIELD_HEX = "0x7f";
    public static final String SEPARATOR_FIELD_SQL = "%7f";
    
    /**
     * 每个表单元格之间使用的正则表达式字符。
     * 多个表单元格的预期架构：
     * x04 [表格单元格] x05 [发生次数] x04x06x04 [表格单元格] x05 [发生次数] x04
     */
    public static final String SEPARATOR_CELL_RGX = "\\x06";
    
    /**
     * 每个表单元格之间使用的SQL字符。
     * 多个表单元格的预期架构：
     * ％04 [表格单元格]％05 [发生次数]％04％06％04 [表格单元格]％05 [发生次数]％04
     */
    public static final String SEPARATOR_CELL_SQL = "%06";
    public static final String SEPARATOR_CELL_HEX = "0x06";
    
    /**
     * 表单元格和单元格文本的出现次数之间使用的SQL字符。
     * 表格单元格数据的预期模式为％04 [表格单元格]％05 [发生次数]％04
     */
    public static final String SEPARATOR_QTE_SQL = "%05";
    
    /**
     * 表单元格和单元格文本的出现次数之间使用的正则表达式字符。
     * 表格单元格数据的预期模式为x04 [表格单元格] x05 [出现次数] x04
     */
    public static final String SEPARATOR_QTE_RGX = "\\x05";
    public static final String SEPARATOR_QTE_HEX = "0x05";
    
    /**
     * 正则表达式字符包含注入返回的表格单元格。
     * 它允许在解析过程中检测表单元格数据的正确结尾。
     * 表格单元格数据的预期模式为x04 [表格单元格] x05 [出现次数] x04
     */
    public static final String ENCLOSE_VALUE_RGX = "\\x04";
    public static final String ENCLOSE_VALUE_HEX = "0x04";
    
    /**
     * 包含由注入返回的表单元格的SQL字符。
     * 它允许在解析过程中检测表单元格数据的正确结尾。
     * 表格单元格数据的预期模式为％04 [表格单元格]％05 [发生次数]％04
     */
    public static final String ENCLOSE_VALUE_SQL = "%04";
    
    public static final String CALIBRATOR_SQL = "%23";
    public static final String CALIBRATOR_HEX = "0x23";
    
    public static final String LEAD_HEX = "0x53714c69";
    public static final String LEAD = "SqLi";
    public static final String LEAD_IN_SHELL = "${JSQL.LEAD}";
    
    /**
     *正则表达式关键字对应于多行和不区分大小写的匹配。
     */
    public static final String MODE = "(?si)";
    
    /**
     * 正则表达式模式描述表单元格，其中首先包含单元格内容，其次出现次数
     * 单元格文本中的*，以十六进制的保留字符x05分隔。
     * 从x01到x1F的字符范围不是用于分析数据和排除的可打印ASCII字符
     * 解析期间可打印的字符。
     * 表格单元格数据的预期模式为x04 [表格单元格] x05 [出现次数] x04
     */
    public static final String CELL_TABLE = "([^\\x01-\\x09\\x0B-\\x0C\\x0E-\\x1F]*)"+ SEPARATOR_QTE_RGX +"([^\\x01-\\x09\\x0B-\\x0C\\x0E-\\x1F]*)(\\x08)?";
    
    private InjectionModel injectionModel;
    
    // Utility class
    public DataAccess(InjectionModel injectionModel) {
        this.injectionModel = injectionModel;
    }
    
    /**
     * 获取常规数据库信息。<br>
      * =>版本{％}数据库{％}用户{％} CURRENT_USER
     * @throws JSqlException
     */
    public void getDatabaseInfos() throws JSqlException {
        LOGGER.trace(I18n.valueByKey("LOG_FETCHING_INFORMATIONS"));
        
        String[] sourcePage = {""};

        String resultToParse;
        resultToParse = new SuspendableGetRows(this.injectionModel).run(
            this.injectionModel.getMediatorVendor().getVendor().instance().sqlInfos(),
            sourcePage,
            false,
            0,
            null
        );

        if ("".equals(resultToParse)) {
            this.injectionModel.sendResponseFromSite("Incorrect database informations", sourcePage[0].trim());
        }

        // 检查解析是否失败
        try {
            this.injectionModel.setDatabaseInfos(
                resultToParse.split(ENCLOSE_VALUE_RGX)[0].replaceAll("\\s+"," "),
                resultToParse.split(ENCLOSE_VALUE_RGX)[1],
                resultToParse.split(ENCLOSE_VALUE_RGX)[2]
            );
        } catch (ArrayIndexOutOfBoundsException e) {
            LOGGER.warn("Incorrect or incomplete data: "+ resultToParse, e);
            LOGGER.info("Processing but failure is expected...");
        }
        
        LOGGER.debug(this.injectionModel.getDatabaseInfos());
    }
    
    /**
     * 获取数据库名称和表计数，并将它们发送到视图。<br>
      *使用可读的文本（非六进制）并解析此模式：<br>
      * => hh [数据库名称1] jj [表计数] hhgghh [数据库名称2] jj [表计数] hhggh ... hi <br>
      *可以在请求结束之前剪切数据窗口，但该过程有助于获得
      *其余无法访问的数据。 该过程可由用户中断（停止/暂停）。
     * @return 找到的数据库集合
     * @throws JSqlException when injection failure or stopped by user
     */
    public List<Database> listDatabases() throws JSqlException {
        LOGGER.trace(I18n.valueByKey("LOG_FETCHING_DATABASES"));
        
        List<Database> databases = new ArrayList<>();
        
        String resultToParse = "";
        try {
            String[] sourcePage = {""};
            resultToParse = new SuspendableGetRows(this.injectionModel).run(
                this.injectionModel.getMediatorVendor().getVendor().instance().sqlDatabases(),
                sourcePage,
                true,
                0,
                null
            );
        } catch (SlidingException e) {
            LOGGER.warn(e.getMessage(), e);
            // 获取已检索的数据片段，而不是去掉它们
            if (!"".equals(e.getSlidingWindowAllRows())) {
                resultToParse = e.getSlidingWindowAllRows();
            } else if (!"".equals(e.getSlidingWindowCurrentRows())) {
                resultToParse = e.getSlidingWindowCurrentRows();
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }

        // 解析检索到的所有数据
        Matcher regexSearch =
            Pattern
                .compile(
                    MODE
                    + ENCLOSE_VALUE_RGX
                    + CELL_TABLE
                    + ENCLOSE_VALUE_RGX
                )
                .matcher(resultToParse);

        if (!regexSearch.find()) {
            throw new InjectionFailureException();
        }
        
        regexSearch.reset();

        // 根据解析的数据构建数据库对象数组
        while (regexSearch.find()) {
            String databaseName = regexSearch.group(1);
            String tableCount = regexSearch.group(2);

            Database newDatabase = new Database(databaseName, tableCount);
            databases.add(newDatabase);
        }

        Request request = new Request();
        request.setMessage(Interaction.ADD_DATABASES);
        request.setParameters(databases);
        this.injectionModel.sendToViews(request);
        
        return databases;
    }

    /**
     * 获取表名称和行数并将其发送到视图。<br>
      *使用可读的文本（非六进制）并解析此模式：<br>
      * => hh [表名1] jj [行数] hhgghh [表名2] jj [行数] hhggh ... hi <br>
      *可以在请求结束之前剪切数据窗口，但该过程有助于获得
      *其余无法访问的数据。 该过程可由用户中断（停止/暂停）。
     * @param database which contains tables to find
     * @return list of tables found
     * @throws JSqlException when injection failure or stopped by user
     */
    public List<Table> listTables(Database database) throws JSqlException {
        // 如果数据库列表不完整，请重置stoppedByUser
        // 而且有些表仍然可以访问
        this.injectionModel.setIsStoppedByUser(false);
        
        List<Table> tables = new ArrayList<>();
        
        // 通知视图数据库已被使用
        Request requestStartProgress = new Request();
        requestStartProgress.setMessage(Interaction.START_PROGRESS);
        requestStartProgress.setParameters(database);
        this.injectionModel.sendToViews(requestStartProgress);

        String tableCount = Integer.toString(database.getChildCount());
        
        String resultToParse = "";
        try {
            String[] pageSource = {""};
            resultToParse = new SuspendableGetRows(this.injectionModel).run(
                this.injectionModel.getMediatorVendor().getVendor().instance().sqlTables(database),
                pageSource,
                true,
                Integer.parseInt(tableCount),
                database
            );
        } catch (SlidingException e) {
            LOGGER.warn(e.getMessage(), e);
            // 获取已检索的数据片段，而不是去掉它们
            if (!"".equals(e.getSlidingWindowAllRows())) {
                resultToParse = e.getSlidingWindowAllRows();
            } else if (!"".equals(e.getSlidingWindowCurrentRows())) {
                resultToParse = e.getSlidingWindowCurrentRows();
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }

        // 解析我们检索到的所有数据
        Matcher regexSearch =
            Pattern
                .compile(
                    MODE
                    + ENCLOSE_VALUE_RGX
                    + CELL_TABLE
                    + ENCLOSE_VALUE_RGX
                )
                .matcher(resultToParse);
        
        Request requestEndProgress = new Request();
        requestEndProgress.setMessage(Interaction.END_PROGRESS);
        requestEndProgress.setParameters(database);
        this.injectionModel.sendToViews(requestEndProgress);
        
        if (!regexSearch.find()) {
            throw new InjectionFailureException();
        }
        
        regexSearch.reset();
        
        // 根据我们解析的数据构建一个Table对象数组
        while (regexSearch.find()) {
            String tableName = regexSearch.group(1);
            String rowCount  = regexSearch.group(2);
            
            Table newTable = new Table(tableName, rowCount, database);
            tables.add(newTable);
        }
        
        Request requestAddTables = new Request();
        requestAddTables.setMessage(Interaction.ADD_TABLES);
        requestAddTables.setParameters(tables);
        this.injectionModel.sendToViews(requestAddTables);
        
        return tables;
    }

    /**
     * 获取列名并将其发送到视图。<br>
      *使用可读的文本（非六进制），并将第二个成员强制设置为31（ASCII中为1）来解析此模式：<br>
      * => hh [列名1] jj [31] hhgghh [列名2] jj [31] hhggh ... hi <br>
      *可以在请求结束之前剪切数据窗口，但该过程有助于获得
      *其余无法访问的数据。 该过程可由用户中断（停止/暂停）。
     * @param 包含要查找的列的表
     * @return 找到的列集合
     * @throws JSqlException when injection failure or stopped by user
     */
    public List<Column> listColumns(Table table) throws JSqlException {
        List<Column> columns = new ArrayList<>();
        
        // Inform the view that table has just been used
        Request requestStartProgress = new Request();
        requestStartProgress.setMessage(Interaction.START_INDETERMINATE_PROGRESS);
        requestStartProgress.setParameters(table);
        this.injectionModel.sendToViews(requestStartProgress);

        String resultToParse = "";
        try {
            String[] pageSource = {""};
            resultToParse = new SuspendableGetRows(this.injectionModel).run(
                this.injectionModel.getMediatorVendor().getVendor().instance().sqlColumns(table),
                pageSource,
                true,
                0,
                table
            );
        } catch (SlidingException e) {
            LOGGER.warn(e.getMessage(), e);
            // 获取已检索的数据片段，而不是去掉它们
            if (!"".equals(e.getSlidingWindowAllRows())) {
                resultToParse = e.getSlidingWindowAllRows();
            } else if (!"".equals(e.getSlidingWindowCurrentRows())) {
                resultToParse = e.getSlidingWindowCurrentRows();
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }

        // 构建SQLite列
        if (this.injectionModel.getMediatorVendor().getVendor() == this.injectionModel.getMediatorVendor().getSqlite()) {
            resultToParse = this.injectionModel.getMediatorVendor().getSqlite().transformSQLite(resultToParse);
        }
        
        // 解析我们检索到的所有数据
        Matcher regexSearch =
            Pattern
                .compile(
                    MODE
                    + ENCLOSE_VALUE_RGX
                    + CELL_TABLE
                    + ENCLOSE_VALUE_RGX
                )
                .matcher(resultToParse);

        Request requestEndProgress = new Request();
        requestEndProgress.setMessage(Interaction.END_INDETERMINATE_PROGRESS);
        requestEndProgress.setParameters(table);
        this.injectionModel.sendToViews(requestEndProgress);

        if (!regexSearch.find()) {
            throw new InjectionFailureException();
        }

        regexSearch.reset();

        // 根据我们解析的数据构建一个Column对象数组
        while (regexSearch.find()) {
            String nameColumn = regexSearch.group(1);

            Column column = new Column(nameColumn, table);
            columns.add(column);
        }

        Request requestAddColumns = new Request();
        requestAddColumns.setMessage(Interaction.ADD_COLUMNS);
        requestAddColumns.setParameters(columns);
        this.injectionModel.sendToViews(requestAddColumns);
        
        return columns;
    }

    /**
     * 获取表值并计算每次出现的次数，然后将其发送到视图。<br>
     * 值使用纯文本（非六进制），并遵循此窗口模式<br>
     * => hh [value 1] jj [count] hhgghh [value 2] jj [count] hhggh ... hi <br>
     * 可以在请求结束之前中断数据窗口，但该过程有助于获得其余无法访问的数据。 该过程可由用户中断（停止/暂停）。
     * @param 用户选择的列
     * @return 一个2x2表，其中包含按列的值
     * @throws JSqlException when injection failure or stopped by user
     */
    public String[][] listValues(List<Column> columns) throws JSqlException {
        Database database = (Database) columns.get(0).getParent().getParent();
        Table table = (Table) columns.get(0).getParent();
        int rowCount = columns.get(0).getParent().getChildCount();

        // Inform the view that table has just been used
        Request request = new Request();
        request.setMessage(Interaction.START_PROGRESS);
        request.setParameters(table);
        this.injectionModel.sendToViews(request);

        // Build an array of column names
        List<String> columnsName = new ArrayList<>();
        for (AbstractElementDatabase e: columns) {
            columnsName.add(e.toString());
        }

        /*
         * From that array, build the SQL fields nicely
         * =>  col1{%}col2...
         * ==> trim(ifnull(`col1`,0x00)),0x7f,trim(ifnull(`Col2`,0x00))...
         */
        String[] arrayColumns = columnsName.toArray(new String[columnsName.size()]);

        String resultToParse = "";
        try {
            String[] pageSource = {""};
            resultToParse = new SuspendableGetRows(this.injectionModel).run(
                this.injectionModel.getMediatorVendor().getVendor().instance().sqlRows(arrayColumns, database, table),
                pageSource,
                true,
                rowCount,
                table
            );
        } catch (SlidingException e) {
            LOGGER.warn(e.getMessage(), e);
            // Get pieces of data already retrieved instead of losing them
            if (!"".equals(e.getSlidingWindowAllRows())) {
                resultToParse = e.getSlidingWindowAllRows();
            } else if (!"".equals(e.getSlidingWindowCurrentRows())) {
                resultToParse = e.getSlidingWindowCurrentRows();
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }

        // Parse all the data we have retrieved
        Matcher regexSearch =
            Pattern
                // TODO requete differente
                .compile(
                    MODE
                    + ENCLOSE_VALUE_RGX
                    + "([^\\x01-\\x09\\x0B-\\x0C\\x0E-\\x1F]*?)"
                    + SEPARATOR_QTE_RGX
                    + "([^\\x01-\\x09\\x0B-\\x0C\\x0E-\\x1F]*?)(\\x08)?"
                    + ENCLOSE_VALUE_RGX
                )
                .matcher(resultToParse);

        if (!regexSearch.find()) {
            throw new InjectionFailureException();
        }
        
        regexSearch.reset();

        int rowsFound = 0;
        List<List<String>> listValues = new ArrayList<>();

        // Build a 2D array of strings from the data we have parsed
        // => row number, occurrence, value1, value2...
        while (regexSearch.find()) {
            String values = regexSearch.group(1);
            int instances = Integer.parseInt(regexSearch.group(2));

            listValues.add(new ArrayList<String>());
            listValues.get(rowsFound).add(Integer.toString(rowsFound + 1));
            listValues.get(rowsFound).add("x"+ instances);
            for (String cellValue: values.split("\\x7F", -1)) {
                listValues.get(rowsFound).add(cellValue);
            }

            rowsFound++;
        }

        // Add the default title to the columns: row number, occurrence
        columnsName.add(0, "");
        columnsName.add(0, "");

        // Build a proper 2D array from the data
        String[][] tableDatas = new String[listValues.size()][columnsName.size()];
        for (int indexRow = 0 ; indexRow < listValues.size() ; indexRow++) {
            boolean isIncomplete = false;
            for (int indexColumn = 0 ; indexColumn < columnsName.size() ; indexColumn++) {
                try {
                    tableDatas[indexRow][indexColumn] = listValues.get(indexRow).get(indexColumn);
                } catch (IndexOutOfBoundsException e) {
                    isIncomplete = true;
                    LOGGER.trace("Incomplete line found");
                    
                    // Ignore
                    IgnoreMessageException exceptionIgnored = new IgnoreMessageException(e);
                    LOGGER.trace(exceptionIgnored, exceptionIgnored);
                }
            }
            if (isIncomplete) {
                LOGGER.warn("String is too long, row #"+ (indexRow + 1) +" is incomplete:");
                LOGGER.warn(String.join(", ", listValues.get(indexRow).toArray(new String[listValues.get(indexRow).size()])));
            }
        }

        arrayColumns = columnsName.toArray(new String[columnsName.size()]);
        
        // Group the columns names, values and Table object in one array
        Object[] objectData = {arrayColumns, tableDatas, table};

        Request requestCreateValuesTab = new Request();
        requestCreateValuesTab.setMessage(Interaction.CREATE_VALUES_TAB);
        requestCreateValuesTab.setParameters(objectData);
        this.injectionModel.sendToViews(requestCreateValuesTab);

        Request requestEndProgress = new Request();
        requestEndProgress.setMessage(Interaction.END_PROGRESS);
        requestEndProgress.setParameters(table);
        this.injectionModel.sendToViews(requestEndProgress);
        
        return tableDatas;
    }
    
}
