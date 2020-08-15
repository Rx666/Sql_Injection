package com.jsql.view.swing;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import com.jsql.i18n.I18n;
import com.jsql.model.InjectionModel;
import com.jsql.model.MediatorModel;
import com.jsql.model.bean.database.AbstractElementDatabase;
import com.jsql.view.interaction.ObserverInteraction;
import com.jsql.view.swing.action.ActionHandler;
import com.jsql.view.swing.menubar.Menubar;
import com.jsql.view.swing.panel.PanelAddressBar;
import com.jsql.view.swing.panel.SplitHorizontalTopBottom;
import com.jsql.view.swing.shadow.ShadowPopupFactory;
import com.jsql.view.swing.shell.AbstractShell;

/**
 *	视图采用MVC模式，定义所有组件及事件。<br>
 * 	主要控件组：<br>
 * - 首部: textfields input<br>
 * - 中间 : 左：tree, 右边：table<br>
 * - 尾部: labels.
 */
@SuppressWarnings("serial")
public class JFrameView extends JFrame {

    /**
     * 	主中心面板，由左右选项卡组成。
     */
    private SplitHorizontalTopBottom splitHorizontalTopBottom;

    /**
     * 	用唯一标识符命令窗口列表
     */
    private Map<UUID, AbstractShell> mapShells = new HashMap<>();

    /**
     * 	将数据库元素与相应的树节点映射。<br>
     *	注入模型将数据库元素发送到视图，然后视图访问其图形组件以进行更新。
     */
    private transient Map<AbstractElementDatabase, DefaultMutableTreeNode> mapNodes = new HashMap<>();
    
    private transient ObserverInteraction observer = new ObserverInteraction("com.jsql.view.swing.interaction");
        
    /**
     * 	创建图形化界面: 应用icon, 功能树icons,3个主要的panel.
     */
    public JFrameView() {
        super("SQL注入检测");//窗口标题
        
        MediatorGui.register(this);
        
        this.setIconImages(HelperUi.getIcons());//窗口图标

        // 在声明其他组件之前加载 UI 
        HelperUi.prepareGUI();
        ShadowPopupFactory.install();
        
        // 主菜单栏
        Menubar menubar = new Menubar();
        this.setJMenuBar(menubar);
        MediatorGui.register(menubar);
        
        // 定义一个默认的panel,每个组件垂直排列
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));

        // 在首部定义Url输入控件
        PanelAddressBar panelAddressBar = new PanelAddressBar();
        this.add(panelAddressBar);
        MediatorGui.register(panelAddressBar);

        // 主界面
        JPanel mainPanel = new JPanel(new GridLayout(1, 0));
        this.splitHorizontalTopBottom = new SplitHorizontalTopBottom();
        mainPanel.add(this.splitHorizontalTopBottom);
        this.add(mainPanel);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Preferences prefs = Preferences.userRoot().node(InjectionModel.class.getName());
                prefs.putInt(
                    SplitHorizontalTopBottom.getNameVSplitpane(),
                    JFrameView.this.splitHorizontalTopBottom.getSplitVerticalLeftRight().getDividerLocation()
                );
                
                // 窗口最大化时分隔线的位置更改
                prefs.putInt(
                    SplitHorizontalTopBottom.getNameHSplitpane(),
                    JFrameView.this.splitHorizontalTopBottom.getHeight() - JFrameView.this.splitHorizontalTopBottom.getDividerLocation()
                );
                
                prefs.putBoolean(HelperUi.BINARY_VISIBLE, false);
                prefs.putBoolean(HelperUi.CHUNK_VISIBLE, false);
                prefs.putBoolean(HelperUi.NETWORK_VISIBLE, false);
                prefs.putBoolean(HelperUi.JAVA_VISIBLE, false);
                
                for (int i = 0 ; i < MediatorGui.tabConsoles().getTabCount() ; i++) {
                    if ("CONSOLE_BINARY_LABEL".equals(MediatorGui.tabConsoles().getTabComponentAt(i).getName())) {
                        prefs.putBoolean(HelperUi.BINARY_VISIBLE, true);
                    } else if ("CONSOLE_CHUNK_LABEL".equals(MediatorGui.tabConsoles().getTabComponentAt(i).getName())) {
                        prefs.putBoolean(HelperUi.CHUNK_VISIBLE, true);
                    } else if ("CONSOLE_NETWORK_LABEL".equals(MediatorGui.tabConsoles().getTabComponentAt(i).getName())) {
                        prefs.putBoolean(HelperUi.NETWORK_VISIBLE, true);
                    } else if ("CONSOLE_JAVA_LABEL".equals(MediatorGui.tabConsoles().getTabComponentAt(i).getName())) {
                        prefs.putBoolean(HelperUi.JAVA_VISIBLE, true);
                    }
                }
            }
        });
        
        this.applyComponentOrientation(ComponentOrientation.getOrientation(I18n.getLocaleDefault()));
        
        // 窗口大小
        this.setSize(1024, 768);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // 窗口居中
        this.setLocationRelativeTo(null);

        //定义选项卡的关键字快捷键＃即使焦点不在选项卡上也能生效
        ActionHandler.addShortcut(this.getRootPane(), MediatorGui.tabResults());
        ActionHandler.addTextFieldShortcutSelectAll();
        
        menubar.switchLocale(Locale.ENGLISH, I18n.getLocaleDefault(), true);
    }

    /**
     * 重置接口
     */
    public void resetInterface() {
        if (MediatorModel.model().getMediatorVendor().getVendorByUser() == MediatorModel.model().getMediatorVendor().getAuto()) {
            MediatorGui.panelAddressBar().getMenuVendor().setText(MediatorModel.model().getMediatorVendor().getAuto().toString());
        }
        MediatorGui.panelAddressBar().getGroupStrategy().clearSelection();
        
        this.mapNodes.clear();
        this.mapShells.clear();
        
        MediatorGui.panelConsoles().reset();
        
        
        for (int i = 0 ; i < MediatorGui.tabConsoles().getTabCount() ; i++) {
            Component tabComponent = MediatorGui.tabConsoles().getTabComponentAt(i);
            if (tabComponent != null) {
                tabComponent.setFont(tabComponent.getFont().deriveFont(Font.PLAIN));
            }
        }
        
    }
    
    // Getters ans setters

    /**
     * 获取命令窗口映射列表
     * @return Map of key/value UUID => Terminal
     */
    public final Map<UUID, AbstractShell> getConsoles() {
        return this.mapShells;
    }
    
    /**
     * 	数据库树模型
     *  @return Tree model
     */
    public final Map<AbstractElementDatabase, DefaultMutableTreeNode> getTreeNodeModels() {
        return this.mapNodes;
    }

    public ObserverInteraction getObserver() {
        return this.observer;
    }

    public SplitHorizontalTopBottom getSplitHorizontalTopBottom() {
        return this.splitHorizontalTopBottom;
    }
    
}
