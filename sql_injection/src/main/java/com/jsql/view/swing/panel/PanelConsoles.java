package com.jsql.view.swing.panel;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultCaret;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.jsql.i18n.I18n;
import com.jsql.model.InjectionModel;
import com.jsql.model.bean.util.HttpHeader;
import com.jsql.util.StringUtil;
import com.jsql.view.i18n.I18nView;
import com.jsql.view.swing.HelperUi;
import com.jsql.view.swing.MediatorGui;
import com.jsql.view.swing.console.JavaConsoleAdapter;
import com.jsql.view.swing.console.SimpleConsoleAdapter;
import com.jsql.view.swing.console.SwingAppender;
import com.jsql.view.swing.panel.util.HTMLEditorKitTextPaneWrap;
import com.jsql.view.swing.popupmenu.JPopupMenuTable;
import com.jsql.view.swing.scrollpane.JScrollIndicator;
import com.jsql.view.swing.scrollpane.LightScrollPane;
import com.jsql.view.swing.splitpane.JSplitPaneWithZeroSizeDivider;
import com.jsql.view.swing.tab.MouseTabbedPane;
import com.jsql.view.swing.tab.TabConsoles;
import com.jsql.view.swing.text.JPopupTextArea;
import com.jsql.view.swing.text.JTextAreaPlaceholderConsole;
import com.jsql.view.swing.text.JTextPanePlaceholder;
import com.jsql.view.swing.text.JToolTipI18n;
import com.jsql.view.swing.ui.CustomMetalTabbedPaneUI;

/**
 * 	底部显示带有不同控制台的面板
 */
@SuppressWarnings("serial")
public class PanelConsoles extends JPanel {
    
    private static final Logger LOGGER = Logger.getRootLogger();
    
    /**
     *	 默认应用程序消息的控制台
     */
    public static final SimpleConsoleAdapter CONSOLE_TEXT_PANE = new SimpleConsoleAdapter("Console", "Event logging");

    /**
     * Java异常消息的控制
     */
    public static final JavaConsoleAdapter JAVA_TEXT_PANE = new JavaConsoleAdapter("Java", "Java unhandled exception");
    
    /**
     * 	原始SQL结果的控制台
     */
    private JTextArea chunkTextArea;

    /**
     * 	该面板显示HTTP请求和响应表
     */
    private JSplitPaneWithZeroSizeDivider network;

    /**
     * 	盲注/时间注入发现的字符的二进制表示的控制台
     */
    private JTextArea binaryTextArea;

    /**
     * HTTP注入请求和响应集合
     */
    private transient List<HttpHeader> listHttpHeader = new ArrayList<>();

    /**
     * "网络"选项卡中的表显示HTTP请求。
     */
    private JTable networkTable;

    private static final JTextArea NETWORK_TAB_URL = new JPopupTextArea("Request URL").getProxy();
    private static final JTextArea NETWORK_TAB_RESPONSE = new JPopupTextArea("Header server response").getProxy();
    private static final JTextArea NETWORK_TAB_SOURCE = new JPopupTextArea("Raw page source").getProxy();
    private static final JTextPane NETWORK_TAB_PREVIEW = new JTextPanePlaceholder("Web browser rendering");
    private static final JTextArea NETWORK_TAB_HEADER = new JPopupTextArea("Header client request").getProxy();
    private static final JTextArea NETWORK_TAB_PARAM = new JPopupTextArea("HTTP POST parameters").getProxy();
    
    private static final BasicArrowButton BUTTON_SHOW_SOUTH = new BasicArrowButton(SwingConstants.SOUTH);
    private static final BasicArrowButton BUTTON_SHOW_NORTH = new BasicArrowButton(SwingConstants.NORTH);
    private static int loc = 0;
    
    private static final MouseTabbedPane NETWORK_TAB_DETAIL = new MouseTabbedPane();
    
    /**
     * 	在底部使用不同的控制台创建面板以报告注入过程
     */
    public PanelConsoles() {
        
        // 自定义的创建对象
        PanelConsoles.CONSOLE_TEXT_PANE.getProxy().setEditable(false);
        SwingAppender.register(PanelConsoles.CONSOLE_TEXT_PANE);
        
        this.chunkTextArea = new JPopupTextArea(new JTextAreaPlaceholderConsole("Raw data extracted during injection")).getProxy();
        this.chunkTextArea.setEditable(false);
        
        this.binaryTextArea = new JPopupTextArea(new JTextAreaPlaceholderConsole("Characters extracted during blind or time injection")).getProxy();
        this.binaryTextArea.setEditable(false);
        
        PanelConsoles.JAVA_TEXT_PANE.getProxy().setEditable(false);
        SwingAppender.register(PanelConsoles.JAVA_TEXT_PANE);
        
        this.network = new JSplitPaneWithZeroSizeDivider(JSplitPane.HORIZONTAL_SPLIT);
        this.network.setResizeWeight(1);
        this.network.setDividerSize(0);
        this.network.setDividerLocation(600);
        this.network.setBorder(BorderFactory.createEmptyBorder());
        this.networkTable = new JTable(0, 4) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        this.networkTable.setComponentPopupMenu(new JPopupMenuTable(this.networkTable));
        this.networkTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        this.networkTable.setRowSelectionAllowed(true);
        this.networkTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.networkTable.setRowHeight(20);
        this.networkTable.setGridColor(Color.LIGHT_GRAY);
        this.networkTable.getTableHeader().setReorderingAllowed(false);
        
        this.networkTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                PanelConsoles.this.networkTable.requestFocusInWindow();
                // 移动选定的行并将光标放在获得焦点的单元格上
                if (SwingUtilities.isRightMouseButton(e)) {
                    Point p = e.getPoint();

                    // 获取包含该坐标的行索引
                    int rowNumber = PanelConsoles.this.networkTable.rowAtPoint(p);
                    int colNumber = PanelConsoles.this.networkTable.columnAtPoint(p);
                    // 获取JTable的ListSelectionModel
                    DefaultListSelectionModel  model = (DefaultListSelectionModel) PanelConsoles.this.networkTable.getSelectionModel();
                    DefaultListSelectionModel  model2 = (DefaultListSelectionModel) PanelConsoles.this.networkTable.getColumnModel().getSelectionModel();

                    PanelConsoles.this.networkTable.setRowSelectionInterval(rowNumber, rowNumber);
                    model.moveLeadSelectionIndex(rowNumber);
                    model2.moveLeadSelectionIndex(colNumber);
                }
            }
        });

        this.networkTable.setModel(new DefaultTableModel() {
            
            private String[] columns = {
                I18n.valueByKey("NETWORK_TAB_METHOD_COLUMN"),
                I18n.valueByKey("NETWORK_TAB_URL_COLUMN"),
                I18n.valueByKey("NETWORK_TAB_SIZE_COLUMN"),
                I18n.valueByKey("NETWORK_TAB_TYPE_COLUMN")
            };

            @Override
            public int getColumnCount() {
                return this.columns.length;
            }

            @Override
            public String getColumnName(int index) {
                return this.columns[index];
            }
            
        });

        class PathCellRenderer extends DefaultTableCellRenderer {
            
            @Override
            public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
            ) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setToolTipText(
                    "<html><div style=\"font-size:10px;font-family:'Ubuntu Mono'\">"
                    + c.getText().replaceAll("(.{100})(?!$)", "$1<br>")
                    + "</div></html>"
                );
                
                return c;
            }
            
        }
        
        this.networkTable.getColumnModel().getColumn(1).setCellRenderer(new PathCellRenderer());
        
        class CenterRenderer extends DefaultTableCellRenderer {
            public CenterRenderer() {
                this.setHorizontalAlignment(SwingConstants.CENTER);
            }
        }

        DefaultTableCellRenderer centerHorizontalAlignment = new CenterRenderer();
        this.networkTable.getColumnModel().getColumn(2).setCellRenderer(centerHorizontalAlignment);
        this.networkTable.getColumnModel().getColumn(3).setCellRenderer(centerHorizontalAlignment);
        
        this.networkTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), null);
        this.networkTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), null);
        
        Set<AWTKeyStroke> forward = new HashSet<>(this.networkTable.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forward.add(KeyStroke.getKeyStroke("TAB"));
        this.networkTable.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forward);
        Set<AWTKeyStroke> backward = new HashSet<>(this.networkTable.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        backward.add(KeyStroke.getKeyStroke("shift TAB"));
        this.networkTable.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backward);
        
        final TableCellRenderer tcrOs = this.networkTable.getTableHeader().getDefaultRenderer();
        this.networkTable.getTableHeader().setDefaultRenderer(
            (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) -> {
                JLabel lbl = (JLabel) tcrOs.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(0, 5, 0, 5)
                    )
                );
                return lbl;
            }
        );
        
        JScrollIndicator scrollerNetwork = new JScrollIndicator(this.networkTable);
        scrollerNetwork.scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, -1, -1));
        scrollerNetwork.scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(0, 0, -1, -1));
        
        AdjustmentListener singleItemScroll = adjustmentEvent -> {
            // 用户滚动列表（使用条，鼠标滚轮或其他工具）：
            if (adjustmentEvent.getAdjustmentType() == AdjustmentEvent.TRACK){
                // 跳到下一个"block"（一行）。
                adjustmentEvent.getAdjustable().setBlockIncrement(100);
                adjustmentEvent.getAdjustable().setUnitIncrement(100);
            }
        };

        scrollerNetwork.scrollPane.getVerticalScrollBar().addAdjustmentListener(singleItemScroll);
        scrollerNetwork.scrollPane.getHorizontalScrollBar().addAdjustmentListener(singleItemScroll);
        
        this.network.setLeftComponent(scrollerNetwork);
        
        NETWORK_TAB_DETAIL.setUI(new CustomMetalTabbedPaneUI() {
            @Override protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                return Math.max(65, super.calculateTabWidth(tabPlacement, tabIndex, metrics));
            }
        });
        
        NETWORK_TAB_DETAIL.addTab(I18n.valueByKey("NETWORK_TAB_URL_LABEL"), new LightScrollPane(1, 1, 0, 0, PanelConsoles.NETWORK_TAB_URL));
        JLabel labelConsoles = new JLabel(I18n.valueByKey("NETWORK_TAB_URL_LABEL"));
        NETWORK_TAB_DETAIL.setTabComponentAt(
            NETWORK_TAB_DETAIL.indexOfTab(I18n.valueByKey("NETWORK_TAB_URL_LABEL")),
            labelConsoles
        );
        I18nView.addComponentForKey("NETWORK_TAB_URL_LABEL", labelConsoles);
        
        NETWORK_TAB_DETAIL.addTab(I18n.valueByKey("NETWORK_TAB_RESPONSE_LABEL"), new LightScrollPane(1, 1, 0, 0, PanelConsoles.NETWORK_TAB_RESPONSE));
        JLabel labelConsoleq = new JLabel(I18n.valueByKey("NETWORK_TAB_RESPONSE_LABEL"));
        NETWORK_TAB_DETAIL.setTabComponentAt(
            NETWORK_TAB_DETAIL.indexOfTab(I18n.valueByKey("NETWORK_TAB_RESPONSE_LABEL")),
            labelConsoleq
        );
        I18nView.addComponentForKey("NETWORK_TAB_RESPONSE_LABEL", labelConsoleq);
        
        PanelConsoles.NETWORK_TAB_PREVIEW.setEditorKit(new HTMLEditorKitTextPaneWrap());
        
        NETWORK_TAB_DETAIL.addTab(I18n.valueByKey("NETWORK_TAB_SOURCE_LABEL"), new LightScrollPane(1, 1, 0, 0, PanelConsoles.NETWORK_TAB_SOURCE));
        JLabel labelConsoleds = new JLabel(I18n.valueByKey("NETWORK_TAB_SOURCE_LABEL"));
        NETWORK_TAB_DETAIL.setTabComponentAt(
            NETWORK_TAB_DETAIL.indexOfTab(I18n.valueByKey("NETWORK_TAB_SOURCE_LABEL")),
            labelConsoleds
        );
        I18nView.addComponentForKey("NETWORK_TAB_SOURCE_LABEL", labelConsoleds);
        
        NETWORK_TAB_DETAIL.addTab(I18n.valueByKey("NETWORK_TAB_PREVIEW_LABEL"), new LightScrollPane(1, 1, 0, 0, PanelConsoles.NETWORK_TAB_PREVIEW));
        JLabel labelConsoleaz = new JLabel(I18n.valueByKey("NETWORK_TAB_PREVIEW_LABEL"));
        NETWORK_TAB_DETAIL.setTabComponentAt(
            NETWORK_TAB_DETAIL.indexOfTab(I18n.valueByKey("NETWORK_TAB_PREVIEW_LABEL")),
            labelConsoleaz
        );
        I18nView.addComponentForKey("NETWORK_TAB_PREVIEW_LABEL", labelConsoleaz);
        
        NETWORK_TAB_DETAIL.addTab(I18n.valueByKey("NETWORK_TAB_HEADERS_LABEL"), new LightScrollPane(1, 1, 0, 0, PanelConsoles.NETWORK_TAB_HEADER));
        JLabel labelConsolexw = new JLabel(I18n.valueByKey("NETWORK_TAB_HEADERS_LABEL"));
        NETWORK_TAB_DETAIL.setTabComponentAt(
            NETWORK_TAB_DETAIL.indexOfTab(I18n.valueByKey("NETWORK_TAB_HEADERS_LABEL")),
            labelConsolexw
        );
        I18nView.addComponentForKey("NETWORK_TAB_HEADERS_LABEL", labelConsolexw);
        
        NETWORK_TAB_DETAIL.addTab(I18n.valueByKey("NETWORK_TAB_PARAMS_LABEL"), new LightScrollPane(1, 1, 0, 0, PanelConsoles.NETWORK_TAB_PARAM));
        JLabel labelConsolvbne = new JLabel(I18n.valueByKey("NETWORK_TAB_PARAMS_LABEL"));
        NETWORK_TAB_DETAIL.setTabComponentAt(
            NETWORK_TAB_DETAIL.indexOfTab(I18n.valueByKey("NETWORK_TAB_PARAMS_LABEL")),
            labelConsolvbne
        );
        I18nView.addComponentForKey("NETWORK_TAB_PARAMS_LABEL", labelConsolvbne);
        
        DefaultCaret caret = (DefaultCaret) PanelConsoles.NETWORK_TAB_RESPONSE.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        caret = (DefaultCaret) PanelConsoles.NETWORK_TAB_SOURCE.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        caret = (DefaultCaret) PanelConsoles.NETWORK_TAB_PREVIEW.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        caret = (DefaultCaret) PanelConsoles.NETWORK_TAB_HEADER.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        caret = (DefaultCaret) PanelConsoles.NETWORK_TAB_PARAM.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        caret = (DefaultCaret) PanelConsoles.NETWORK_TAB_URL.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        
        PanelConsoles.NETWORK_TAB_HEADER.setLineWrap(true);
        PanelConsoles.NETWORK_TAB_PARAM.setLineWrap(true);
        PanelConsoles.NETWORK_TAB_RESPONSE.setLineWrap(true);
        PanelConsoles.NETWORK_TAB_URL.setLineWrap(true);
        PanelConsoles.NETWORK_TAB_SOURCE.setLineWrap(true);
        
        PanelConsoles.NETWORK_TAB_PREVIEW.setContentType("text/html");
        PanelConsoles.NETWORK_TAB_PREVIEW.setEditable(false);
        
        this.networkTable.getColumnModel().getColumn(0).setPreferredWidth(75);
        this.networkTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        this.networkTable.getColumnModel().getColumn(2).setPreferredWidth(40);
        this.networkTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        
        this.networkTable.getSelectionModel().addListSelectionListener(event -> {
            // 防止重复事件
            if (!event.getValueIsAdjusting() && PanelConsoles.this.networkTable.getSelectedRow() > -1) {
                this.changeTextNetwork();
            }
        });

        this.network.setRightComponent(NETWORK_TAB_DETAIL);

        MediatorGui.register(new TabConsoles());
        MediatorGui.tabConsoles().setUI(new CustomMetalTabbedPaneUI() {
            @Override protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                return Math.max(80, super.calculateTabWidth(tabPlacement, tabIndex, metrics));
            }
        });
        MediatorGui.tabConsoles().setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));

        this.buildI18nTab(
            "CONSOLE_MAIN_LABEL",
            "CONSOLE_MAIN_TOOLTIP",
            HelperUi.ICON_CONSOLE,
            new LightScrollPane(1, 0, 0, 0, PanelConsoles.CONSOLE_TEXT_PANE.getProxy()),
            0
        );

        // 这里顺序很重要
        Preferences prefs = Preferences.userRoot().node(InjectionModel.class.getName());
        if (prefs.getBoolean(HelperUi.JAVA_VISIBLE, false)) {
            this.insertJavaTab();
        }
        if (prefs.getBoolean(HelperUi.NETWORK_VISIBLE, true)) {
            this.insertNetworkTab();
        }
        
        MediatorGui.tabConsoles().addChangeListener(changeEvent -> {
            JTabbedPane tabs = MediatorGui.tabConsoles();
            if (tabs.getSelectedIndex() > -1) {
                Component currentTabHeader = tabs.getTabComponentAt(tabs.getSelectedIndex());
                if (currentTabHeader != null) {
                    currentTabHeader.setFont(currentTabHeader.getFont().deriveFont(Font.PLAIN));
                    currentTabHeader.setForeground(Color.BLACK);
                }
            }
        });

        this.setLayout(new OverlayLayout(this));

        PanelConsoles.BUTTON_SHOW_SOUTH.setBorderPainted(false);
        PanelConsoles.BUTTON_SHOW_SOUTH.setPreferredSize(new Dimension(PanelConsoles.BUTTON_SHOW_SOUTH.getPreferredSize().width, PanelConsoles.BUTTON_SHOW_SOUTH.getPreferredSize().height));
        PanelConsoles.BUTTON_SHOW_SOUTH.setMaximumSize(PanelConsoles.BUTTON_SHOW_SOUTH.getPreferredSize());
        PanelConsoles.BUTTON_SHOW_SOUTH.setOpaque(false);
        PanelConsoles.BUTTON_SHOW_SOUTH.setBorder(BorderFactory.createEmptyBorder());
        PanelConsoles.BUTTON_SHOW_SOUTH.addActionListener(SplitHorizontalTopBottom.getActionHideShowConsole());
        
        PanelConsoles.BUTTON_SHOW_NORTH.setBorderPainted(false);
        PanelConsoles.BUTTON_SHOW_NORTH.setPreferredSize(new Dimension(PanelConsoles.BUTTON_SHOW_NORTH.getPreferredSize().width, PanelConsoles.BUTTON_SHOW_NORTH.getPreferredSize().height));
        PanelConsoles.BUTTON_SHOW_NORTH.setMaximumSize(PanelConsoles.BUTTON_SHOW_NORTH.getPreferredSize());
        PanelConsoles.BUTTON_SHOW_NORTH.setOpaque(false);
        PanelConsoles.BUTTON_SHOW_NORTH.setBorder(BorderFactory.createEmptyBorder());
        PanelConsoles.BUTTON_SHOW_NORTH.addActionListener(SplitHorizontalTopBottom.getActionHideShowResult());

        JPanel arrowDownPanel = new JPanel();
        arrowDownPanel.setLayout(new BorderLayout());
        arrowDownPanel.setOpaque(false);
        // 禁用与zerosizesplitter的重叠
        arrowDownPanel.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
        arrowDownPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 26));
        arrowDownPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JPanel panelExpander = new JPanel(new BorderLayout());
        panelExpander.setBorder(BorderFactory.createEmptyBorder());
        panelExpander.add(PanelConsoles.BUTTON_SHOW_SOUTH, BorderLayout.SOUTH);
        panelExpander.add(PanelConsoles.BUTTON_SHOW_NORTH, BorderLayout.NORTH);
        arrowDownPanel.add(panelExpander, BorderLayout.LINE_END);
        
        this.add(arrowDownPanel);
        this.add(MediatorGui.tabConsoles());

        // 做叠加
        arrowDownPanel.setAlignmentX(FlowLayout.TRAILING);
        arrowDownPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        MediatorGui.tabConsoles().setAlignmentX(FlowLayout.LEADING);
        MediatorGui.tabConsoles().setAlignmentY(Component.TOP_ALIGNMENT);

        this.chunkTextArea.setLineWrap(true);
        this.binaryTextArea.setLineWrap(true);
    }


    /**
     * 添加网络选项卡添加到底部面板
     */
    public void insertNetworkTab() {
        this.buildI18nTab(
            "CONSOLE_NETWORK_LABEL",
            "CONSOLE_NETWORK_TOOLTIP",
            HelperUi.ICON_HEADER,
            new LightScrollPane(1, 0, 0, 0, PanelConsoles.this.network),
            MediatorGui.tabConsoles().getTabCount() - (MediatorGui.menubar().getJavaDebugMenu().isSelected() ? 1 : 0)
        );
    }

    /**
     * 添加Java选项卡添加到底部面板
     */
    public void insertJavaTab() {
        this.buildI18nTab(
            "CONSOLE_JAVA_LABEL",
            "CONSOLE_JAVA_TOOLTIP",
            HelperUi.ICON_CUP,
            new LightScrollPane(1, 0, 0, 0, PanelConsoles.JAVA_TEXT_PANE.getProxy()),
            MediatorGui.tabConsoles().getTabCount()
        );
    }
    
    public void addHeader(HttpHeader header) {
        this.listHttpHeader.add(header);
    }
    
    public void reset() {
        this.listHttpHeader.clear();
        
        // 清空标签信息
        this.getChunkTab().setText("");
        this.getBinaryTab().setText("");
        
        try {
            ((DefaultTableModel) this.networkTable.getModel()).setRowCount(0);
        } catch(NullPointerException | ArrayIndexOutOfBoundsException e) {
            LOGGER.error(e.getMessage(), e);
        }
        
        PanelConsoles.JAVA_TEXT_PANE.getProxy().setText("");
        
        PanelConsoles.NETWORK_TAB_URL.setText("");
        PanelConsoles.NETWORK_TAB_HEADER.setText("");
        PanelConsoles.NETWORK_TAB_PARAM.setText("");
        PanelConsoles.NETWORK_TAB_RESPONSE.setText("");
        
        try {
            PanelConsoles.NETWORK_TAB_SOURCE.setText("");
        } catch (NullPointerException e) {
            LOGGER.error(e, e);
        }
        
        try {
            PanelConsoles.NETWORK_TAB_PREVIEW.setText("");
        } catch (ArrayIndexOutOfBoundsException e) {
            LOGGER.error(e, e);
        }
    }
    
    public void changeTextNetwork() {
        HttpHeader networkData = this.listHttpHeader.get(PanelConsoles.this.networkTable.getSelectedRow());
        PanelConsoles.NETWORK_TAB_HEADER.setText(networkData.getHeader());
        PanelConsoles.NETWORK_TAB_PARAM.setText(networkData.getPost());
        PanelConsoles.NETWORK_TAB_URL.setText(networkData.getUrl());
        
        PanelConsoles.NETWORK_TAB_RESPONSE.setText("");
        for (String key: networkData.getResponse().keySet()) {
            PanelConsoles.NETWORK_TAB_RESPONSE.append(key + ": " + networkData.getResponse().get(key));
            PanelConsoles.NETWORK_TAB_RESPONSE.append("\n");
        }
        
        try {
            PanelConsoles.NETWORK_TAB_SOURCE.setText(
                StringUtil.detectUtf8(networkData.getSource())
                    .replaceAll("#{5,}", "#*")
                    .trim()
            );
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            LOGGER.error(e, e);
        }
        
        // 重置EditorKit以禁用先前的文档效果
        PanelConsoles.NETWORK_TAB_PREVIEW.getEditorKit().createDefaultDocument();
        
        // jsoup使用代理来显示<img>标签之前用于2xx成功和3xx重定向的测试仅是Header，现在获取HTML内容
        try {
            PanelConsoles.NETWORK_TAB_PREVIEW.setText(
                Jsoup.clean(
                    "<html>"+ StringUtil.detectUtf8(networkData.getSource()).replaceAll("#{5,}", "#*") + "</html>"
                        .replaceAll("<img.*>", "")
                        .replaceAll("<input.*type=\"?hidden\"?.*>", "")
                        .replaceAll("<input.*type=\"?(submit|button)\"?.*>", "<div style=\"background-color:#eeeeee;text-align:center;border:1px solid black;width:100px;\">button</div>")
                        .replaceAll("<input.*>", "<div style=\"text-align:center;border:1px solid black;width:100px;\">input</div>"),
                    Whitelist.relaxed()
                        .addTags("center", "div", "span")
                        .addAttributes(":all", "style")
                )
            );
        } catch (RuntimeException | ExceptionInInitializerError e) {
            LOGGER.error(e, e);
        }
    }
    
    private void buildI18nTab(
        String keyLabel,
        String keyTooltip,
        Icon icon,
        Component manager,
        int position
    ) {
        final JToolTipI18n[] j = new JToolTipI18n[]{new JToolTipI18n(I18nView.valueByKey(keyTooltip))};
        JLabel labelTab = new JLabel(I18nView.valueByKey(keyLabel), icon, SwingConstants.CENTER){
            @Override
            public JToolTip createToolTip() {
                JToolTip tipI18n = new JToolTipI18n(I18nView.valueByKey(keyTooltip));
                j[0] = (JToolTipI18n) tipI18n;
                return tipI18n;
            }
        };
        labelTab.setName(keyLabel);
        labelTab.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                MediatorGui.tabConsoles().setSelectedComponent(manager);
                super.mousePressed(e);
            }
        });
        MediatorGui.tabConsoles().insertTab(I18nView.valueByKey(keyLabel), icon, manager, null, position);
        MediatorGui.tabConsoles().setTabComponentAt(
            MediatorGui.tabConsoles().indexOfTab(I18nView.valueByKey(keyLabel)),
            labelTab
        );
        I18nView.addComponentForKey(keyLabel, labelTab);
        I18nView.addComponentForKey(keyTooltip, j[0]);
        labelTab.setToolTipText(I18nView.valueByKey(keyTooltip));
    }
    
    // Getter and setter

    public JTextArea getChunkTab() {
        return this.chunkTextArea;
    }

    public JSplitPaneWithZeroSizeDivider getNetwork() {
        return this.network;
    }

    public JTextArea getBinaryTab() {
        return this.binaryTextArea;
    }

    public static int getLoc() {
        return loc;
    }

    public static void setLoc(int loc) {
        PanelConsoles.loc = loc;
    }

    public static BasicArrowButton getButtonShowNorth() {
        return BUTTON_SHOW_NORTH;
    }

    public JTable getNetworkTable() {
        return this.networkTable;
    }
    
}
