
package com.jsql.view.swing.menubar;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.MenuSelectionManager;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;

import com.jsql.i18n.I18n;
import com.jsql.mapper.UrlMapper;
import com.jsql.model.InjectionModel;
import com.jsql.model.MediatorModel;
import com.jsql.model.bean.SubUrl;
import com.jsql.model.bean.Url;
import com.jsql.model.bean.excel.ExcelUrlBean;
import com.jsql.util.CVSSUtil;
import com.jsql.util.HtmlUtil;
import com.jsql.util.SpringContextUtils;
import com.jsql.util.excel.ExcelUtils;
import com.jsql.view.i18n.I18nView;
import com.jsql.view.swing.HelperUi;
import com.jsql.view.swing.MediatorGui;
import com.jsql.view.swing.action.ActionHandler;
import com.jsql.view.swing.action.ActionNewWindow;
import com.jsql.view.swing.action.ActionSaveTab;
import com.jsql.view.swing.console.SwingAppender;
import com.jsql.view.swing.dialog.DialogAbout;
import com.jsql.view.swing.interaction.CreateTab;
import com.jsql.view.swing.sql.SqlEngine;
import com.jsql.view.swing.tab.TabHeader;
import com.jsql.view.swing.table.PanelTable;
import com.jsql.view.swing.text.JTextFieldPlaceholder;

/**
 * 应用程序主菜单栏
 */
@SuppressWarnings("serial")
public class Menubar extends JMenuBar {

	/**
	 * Log4j 日志
	 */
	private static final Logger LOGGER = Logger.getRootLogger();

	/**
	 * 复选框项-显示/隐藏网络面板
	 */
	private JCheckBoxMenuItem networkMenu;

	/**
	 * 用于显示/隐藏Java控制台的复选框
	 */
	private JCheckBoxMenuItem javaDebugMenu;

	private UrlMapper urlMapper = SpringContextUtils.getBean(UrlMapper.class);
	
	public static List<ExcelUrlBean> list = null;

	/**
	 * 在主界面创建一个菜单栏。
	 */
	public Menubar() {
		JOptionPane.setDefaultLocale(Locale.CHINESE);
		// 文件 > 保存窗口 | 退出
		JMenu menuFile = new JMenu("File");
		I18nView.addComponentForKey("MENUBAR_FILE", menuFile);
		menuFile.setMnemonic('F');// 设置系统热键alt+F
		// 保存
		JMenuItem itemSave = new JMenuItem(new ActionSaveTab());
		I18nView.addComponentForKey("MENUBAR_FILE_SAVETABAS", itemSave);
		// 导出报告
		JMenuItem itemReport = new JMenuItem("导出报告");
		I18nView.addComponentForKey("MENUBAR_FILE_REPORT", itemReport);
		itemReport.addActionListener(actionEvent -> {
			List<Url> urlList = urlMapper.selectUrlAll();
			for (Url url : urlList) {
				List<SubUrl> subUrlList = urlMapper.selectSubUrlListById(url.getId());
				int bugNum = urlMapper.selectCountBugNumById(url.getId());
				float confImpact = urlMapper.countConfImpactById(url.getId());
				int score = CVSSUtil.score(url.getAccessVector(), CVSSUtil.ACCESS_COMPLEXITY_HIGHT,
						url.getAuthentication().equals("0") ? false : true, confImpact, CVSSUtil.INTEGIMPACT_SOME,
						CVSSUtil.AVAILIMPACT_SOME);
				//String info = "";
				try {
					HtmlUtil.exportHtml(url, subUrlList, score, bugNum);
					//info = "Export Success!";
				} catch (IOException e) {
					e.printStackTrace();
					//info = "Export fail :" + e.getMessage();
				}
			}
			JOptionPane.showMessageDialog(null, "Export Success!", "提示",JOptionPane.INFORMATION_MESSAGE);
		});
		// 批量检测
		JMenuItem batchCheck = new JMenuItem("批量检测");
		I18nView.addComponentForKey("MENUBAR_BATCH_CHECK", batchCheck);
		batchCheck.addActionListener(actionEvent -> {
			JFileChooser jfc = new JFileChooser();
			updateFont(jfc,new Font("宋体",0,12));
			jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			jfc.showDialog(new JLabel(), "选择");
			File file = jfc.getSelectedFile();
			if(file != null) {
				if (file.isDirectory() || !file.getName().contains(".xls") || !file.getName().contains(".xlsx")) {
					//new ShowMessageFrame("请选择excel文件！");
					JOptionPane.showMessageDialog(null, "Not a excel file!", "提示",JOptionPane.WARNING_MESSAGE);
				} else {
					MediatorModel.model().setBatchScan(true);//仅作状态判断使用
					list = ExcelUtils.readExcel(ExcelUrlBean.class, file);
					if(list != null && list.size() > 0) {
						urlMapper.deleteUrl();
						MediatorGui.panelAddressBar().getTextFieldAddress().setText(list.get(0).getUrl());
						MediatorGui.tabManagers().refresh("[{\"url\":\"\"}]");
						MediatorGui.panelAddressBar().getButtonInUrl().doClick();
					}else {
						JOptionPane.showMessageDialog(null, "Excel data is null!", "提示",JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		});
		// 退出
		JMenuItem itemExit = new JMenuItem(I18n.valueByKey("MENUBAR_FILE_EXIT"), 'x');
		I18nView.addComponentForKey("MENUBAR_FILE_EXIT", itemExit);
		itemExit.setIcon(HelperUi.ICON_EMPTY);
		itemExit.addActionListener(actionEvent -> MediatorGui.frame().dispose());
		ActionHandler.addShortcut(Menubar.this);
		menuFile.add(itemSave);
		menuFile.add(new JSeparator());// 分割线
		menuFile.add(itemReport);
		menuFile.add(new JSeparator());// 分割线
		menuFile.add(batchCheck);
		menuFile.add(new JSeparator());// 分割线
		menuFile.add(itemExit);

		// 编辑 > 复制 | 选择所有
		JMenu menuEdit = new JMenu(I18n.valueByKey("MENUBAR_EDIT"));
		I18nView.addComponentForKey("MENUBAR_EDIT", menuEdit);
		menuEdit.setMnemonic('E');
		// 复制
		JMenuItem itemCopy = new JMenuItem(I18n.valueByKey("CONTEXT_MENU_COPY"), 'C');
		I18nView.addComponentForKey("CONTEXT_MENU_COPY", itemCopy);
		itemCopy.setIcon(HelperUi.ICON_EMPTY);//
		itemCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));// 快捷键
		itemCopy.addActionListener(actionEvent -> {
			if (MediatorGui.tabResults().getSelectedComponent() instanceof PanelTable) {
				((PanelTable) MediatorGui.tabResults().getSelectedComponent()).copyTable();
			} else if (MediatorGui.tabResults().getSelectedComponent() instanceof JScrollPane) {
				((JTextArea) ((JScrollPane) MediatorGui.tabResults().getSelectedComponent()).getViewport().getView())
						.copy();
			}
		});
		// 选择所有
		JMenuItem itemSelectAll = new JMenuItem(I18n.valueByKey("CONTEXT_MENU_SELECT_ALL"), 'A');
		I18nView.addComponentForKey("CONTEXT_MENU_SELECT_ALL", itemSelectAll);
		itemSelectAll.setIcon(HelperUi.ICON_EMPTY);
		itemSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		itemSelectAll.addActionListener(actionEvent -> {
			if (MediatorGui.tabResults().getSelectedComponent() instanceof PanelTable) {
				((PanelTable) MediatorGui.tabResults().getSelectedComponent()).selectTable();
				// 选择全部时，Textarea需要获得焦点
			} else if (MediatorGui.tabResults().getSelectedComponent() instanceof JScrollPane) {
				((JScrollPane) MediatorGui.tabResults().getSelectedComponent()).getViewport().getView()
						.requestFocusInWindow();
				((JTextArea) ((JScrollPane) MediatorGui.tabResults().getSelectedComponent()).getViewport().getView())
						.selectAll();
			}
		});
		menuEdit.add(itemCopy);
		menuEdit.add(new JSeparator());// 分割线
		menuEdit.add(itemSelectAll);

		// 窗口 > 新窗口|显示视图|显示面板|sql引擎|优先选项
		JMenu menuWindows = new JMenu(I18n.valueByKey("MENUBAR_WINDOWS"));
		I18nView.addComponentForKey("MENUBAR_WINDOWS", menuWindows);
		menuWindows.setMnemonic('W');
		// 新窗口
		JMenuItem itemNewWindows = new JMenuItem(new ActionNewWindow());
		I18nView.addComponentForKey("NEW_WINDOW_MENU", itemNewWindows);
		menuWindows.add(itemNewWindows);
		menuWindows.add(new JSeparator());

		// 读取注册表的配置信息后，初始化菜单的下拉框选中状态
		Preferences prefs = Preferences.userRoot().node(InjectionModel.class.getName());
		JMenu menuPanel = new JMenu(I18n.valueByKey("MENUBAR_PANEL"));
		I18nView.addComponentForKey("MENUBAR_PANEL", menuPanel);

		// 二级菜单-网络
		this.networkMenu = new JCheckBoxMenuItem(I18n.valueByKey("CONSOLE_NETWORK_LABEL"), HelperUi.ICON_HEADER,
				prefs.getBoolean(HelperUi.NETWORK_VISIBLE, true));
		I18nView.addComponentForKey("CONSOLE_NETWORK_LABEL", this.networkMenu);
		menuPanel.add(this.networkMenu);
		// 二级菜单-java
		this.javaDebugMenu = new JCheckBoxMenuItem(I18n.valueByKey("CONSOLE_JAVA_LABEL"), HelperUi.ICON_CUP,
				prefs.getBoolean(HelperUi.JAVA_VISIBLE, false));
		I18nView.addComponentForKey("CONSOLE_JAVA_LABEL", this.javaDebugMenu);

		for (JCheckBoxMenuItem menuItem : new JCheckBoxMenuItem[] { this.networkMenu, this.javaDebugMenu }) {
			menuItem.setUI(new BasicCheckBoxMenuItemUI() {
				@Override
				protected void doClick(MenuSelectionManager msm) {
					this.menuItem.doClick(0);
				}
			});
		}

		this.networkMenu.addActionListener(actionEvent -> {
			if (this.networkMenu.isSelected()) {
				MediatorGui.panelConsoles().insertNetworkTab();
			} else {
				MediatorGui.tabConsoles().remove(MediatorGui.tabConsoles().indexOfTab(HelperUi.ICON_HEADER));
			}
		});

		this.javaDebugMenu.addActionListener(actionEvent -> {
			if (this.javaDebugMenu.isSelected()) {
				MediatorGui.panelConsoles().insertJavaTab();
			} else {
				MediatorGui.tabConsoles().remove(MediatorGui.tabConsoles().indexOfTab(HelperUi.ICON_CUP));
			}
		});

		menuPanel.add(this.javaDebugMenu);
		menuWindows.add(menuPanel);
		menuWindows.add(new JSeparator());
		// 优先选项
		// JMenuItem preferences = new JMenuItem(I18n.valueByKey("MENUBAR_PREFERENCES"),
		// 'P');
		// preferences.setIcon(HelperUi.ICON_EMPTY);
		// I18nView.addComponentForKey("MENUBAR_PREFERENCES", preferences);

		// 渲染优先选项的场景
//        String titleTabPreferences = "Preferences";
//        preferences.addActionListener(actionEvent -> {
//            for (int i = 0; i < MediatorGui.tabResults().getTabCount() ; i++) {
//                if (titleTabPreferences.equals(MediatorGui.tabResults().getTitleAt(i))) {
//                    MediatorGui.tabResults().setSelectedIndex(i);
//                    return;
//                }
//            }
//            
//            CreateTab.initializeSplitOrientation();
//            
//            AdjustmentListener singleItemScroll = adjustmentEvent -> {
//                // 用户滚动列表（使用条，鼠标滚轮或其他工具）
//                if (adjustmentEvent.getAdjustmentType() == AdjustmentEvent.TRACK){
//                	//跳转到下一个块（一行）
//                    adjustmentEvent.getAdjustable().setBlockIncrement(100);
//                    adjustmentEvent.getAdjustable().setUnitIncrement(100);
//                }
//            };
//
//            LightScrollPane scroller = new LightScrollPane(1, 0, 0, 0, new PanelPreferences());
//            scroller.scrollPane.getVerticalScrollBar().addAdjustmentListener(singleItemScroll);
//            
//            MediatorGui.tabResults().addTab(titleTabPreferences, scroller);
//
//            // 焦点位于新标签
//            MediatorGui.tabResults().setSelectedComponent(scroller);
//
//            // 使用关闭按钮创建自定义标签
//            TabHeader header = new TabHeader(I18nView.valueByKey("MENUBAR_PREFERENCES"), HelperUi.ICON_FILE_SERVER);
//            I18nView.addComponentForKey("MENUBAR_PREFERENCES", header.getTabTitleLabel());
//
//            // 将自定义标签应用于选项卡
//            MediatorGui.tabResults().setTabComponentAt(MediatorGui.tabResults().indexOfComponent(scroller), header);
//        });

		JMenuItem sqlEngine = new JMenuItem(I18n.valueByKey("MENUBAR_SQL_ENGINE"));
		I18nView.addComponentForKey("MENUBAR_SQL_ENGINE", sqlEngine);

		// 渲染sql引擎的场景
		String titleTabSqlEngine = "SQL Engine";
		sqlEngine.addActionListener(actionEvent -> {
			for (int i = 0; i < MediatorGui.tabResults().getTabCount(); i++) {
				if (titleTabSqlEngine.equals(MediatorGui.tabResults().getTitleAt(i))) {
					MediatorGui.tabResults().setSelectedIndex(i);
					return;
				}
			}

			CreateTab.initializeSplitOrientation();

			SqlEngine panelSqlEngine = new SqlEngine();

			MediatorGui.tabResults().addTab(titleTabSqlEngine, panelSqlEngine);

			// 焦点位于新标签
			MediatorGui.tabResults().setSelectedComponent(panelSqlEngine);

			// 使用关闭按钮创建自定义标签
			TabHeader header = new TabHeader(I18nView.valueByKey("MENUBAR_SQL_ENGINE"), HelperUi.ICON_FILE_SERVER);
			I18nView.addComponentForKey("MENUBAR_SQL_ENGINE", header.getTabTitleLabel());

			// 将自定义标签应用于选项卡
			MediatorGui.tabResults().setTabComponentAt(MediatorGui.tabResults().indexOfComponent(panelSqlEngine),
					header);
		});

		menuWindows.add(sqlEngine);
		// menuWindows.add(preferences);

		// 帮助 > 关于
		JMenu menuHelp = new JMenu(I18n.valueByKey("MENUBAR_HELP"));
		menuHelp.setMnemonic('H');
		I18nView.addComponentForKey("MENUBAR_HELP", menuHelp);

		JMenuItem itemHelp = new JMenuItem(I18n.valueByKey("MENUBAR_HELP_ABOUT"), 'A');
		itemHelp.setIcon(HelperUi.ICON_EMPTY);
		I18nView.addComponentForKey("MENUBAR_HELP_ABOUT", itemHelp);

		// 渲染关于场景
		final DialogAbout aboutDiag = new DialogAbout();
		itemHelp.addActionListener(actionEvent -> {
			// 居中对话框
			if (!aboutDiag.isVisible()) {
				aboutDiag.reinit();
				// 这里需要按钮焦点
				aboutDiag.setVisible(true);
				aboutDiag.requestButtonFocus();
			}
			aboutDiag.setVisible(true);
		});
		menuHelp.add(itemHelp);

		// 添加项到menubar
		this.add(menuFile);
		this.add(menuEdit);
		this.add(menuWindows);
		this.add(menuHelp);
	}

	public void switchLocale(Locale newLocale) {
		this.switchLocale(I18n.getLocaleDefault(), newLocale, false);
	}

	public void switchLocale(Locale oldLocale, Locale newLocale, boolean isStartup) {
		I18n.setLocaleDefault(ResourceBundle.getBundle("com.jsql.i18n.jsql", newLocale));

		JTableHeader header = MediatorGui.panelConsoles().getNetworkTable().getTableHeader();
		TableColumnModel colMod = header.getColumnModel();
		if (new Locale("zh").getLanguage().equals(newLocale.getLanguage())
				|| new Locale("ko").getLanguage().equals(newLocale.getLanguage())) {
			StyleConstants.setFontFamily(SwingAppender.ERROR, HelperUi.FONT_NAME_UBUNTU_REGULAR);
			StyleConstants.setFontFamily(SwingAppender.WARN, HelperUi.FONT_NAME_UBUNTU_REGULAR);
			StyleConstants.setFontFamily(SwingAppender.INFO, HelperUi.FONT_NAME_UBUNTU_REGULAR);
			StyleConstants.setFontFamily(SwingAppender.DEBUG, HelperUi.FONT_NAME_UBUNTU_REGULAR);
			StyleConstants.setFontFamily(SwingAppender.TRACE, HelperUi.FONT_NAME_UBUNTU_REGULAR);
			StyleConstants.setFontFamily(SwingAppender.ALL, HelperUi.FONT_NAME_UBUNTU_REGULAR);

			// MediatorGui.managerBruteForce().getResult().setFont(HelperUi.FONT_UBUNTU_REGULAR);

			colMod.getColumn(0).setHeaderValue(I18nView.valueByKey("NETWORK_TAB_METHOD_COLUMN"));
			colMod.getColumn(1).setHeaderValue(I18nView.valueByKey("NETWORK_TAB_URL_COLUMN"));
			colMod.getColumn(2).setHeaderValue(I18nView.valueByKey("NETWORK_TAB_SIZE_COLUMN"));
			colMod.getColumn(3).setHeaderValue(I18nView.valueByKey("NETWORK_TAB_TYPE_COLUMN"));
		} else {
			StyleConstants.setFontFamily(SwingAppender.ERROR, HelperUi.FONT_NAME_UBUNTU_MONO);
			StyleConstants.setFontFamily(SwingAppender.WARN, HelperUi.FONT_NAME_UBUNTU_MONO);
			StyleConstants.setFontFamily(SwingAppender.INFO, HelperUi.FONT_NAME_UBUNTU_MONO);
			StyleConstants.setFontFamily(SwingAppender.DEBUG, HelperUi.FONT_NAME_UBUNTU_MONO);
			StyleConstants.setFontFamily(SwingAppender.TRACE, HelperUi.FONT_NAME_UBUNTU_MONO);
			StyleConstants.setFontFamily(SwingAppender.ALL, HelperUi.FONT_NAME_UBUNTU_MONO);

			MediatorGui.managerBruteForce().getResult().setFont(HelperUi.FONT_UBUNTU_MONO);

			colMod.getColumn(0).setHeaderValue(I18n.valueByKey("NETWORK_TAB_METHOD_COLUMN"));
			colMod.getColumn(1).setHeaderValue(I18n.valueByKey("NETWORK_TAB_URL_COLUMN"));
			colMod.getColumn(2).setHeaderValue(I18n.valueByKey("NETWORK_TAB_SIZE_COLUMN"));
			colMod.getColumn(3).setHeaderValue(I18n.valueByKey("NETWORK_TAB_TYPE_COLUMN"));
		}
		header.repaint();

		for (String key : I18nView.keys()) {
			for (Object componentSwing : I18nView.componentsByKey(key)) {
				Class<?> classComponent = componentSwing.getClass();
				try {
					if (componentSwing instanceof JTextFieldPlaceholder) {
						Method setPlaceholderText = classComponent.getMethod("setPlaceholderText", String.class);
						setPlaceholderText.invoke(componentSwing, I18n.valueByKey(key));
					} else {
						Method methodSetText = classComponent.getMethod("setText", String.class);
						methodSetText.setAccessible(true);
						if (
						// TODO
						new Locale("zh").getLanguage().equals(newLocale.getLanguage())
								|| new Locale("ko").getLanguage().equals(newLocale.getLanguage())) {
							methodSetText.invoke(componentSwing, I18nView.valueByKey(key));
						} else {
							methodSetText.invoke(componentSwing, I18n.valueByKey(key));
						}
					}
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					LOGGER.error("Reflection for " + key + " failed while switching locale", e);
				}
			}
		}

		ComponentOrientation componentOrientation = ComponentOrientation.getOrientation(I18n.getLocaleDefault());
		MediatorGui.frame().applyComponentOrientation(componentOrientation);

		if (ComponentOrientation.getOrientation(oldLocale) != ComponentOrientation.getOrientation(newLocale)) {
			Component c1 = MediatorGui.frame().getSplitHorizontalTopBottom().getSplitVerticalLeftRight()
					.getLeftComponent();
			Component c2 = MediatorGui.frame().getSplitHorizontalTopBottom().getSplitVerticalLeftRight()
					.getRightComponent();

			MediatorGui.frame().getSplitHorizontalTopBottom().getSplitVerticalLeftRight().setLeftComponent(null);
			MediatorGui.frame().getSplitHorizontalTopBottom().getSplitVerticalLeftRight().setRightComponent(null);
			MediatorGui.frame().getSplitHorizontalTopBottom().getSplitVerticalLeftRight().setLeftComponent(c2);
			MediatorGui.frame().getSplitHorizontalTopBottom().getSplitVerticalLeftRight().setRightComponent(c1);

			if (isStartup) {
				MediatorGui.frame().getSplitHorizontalTopBottom().getSplitVerticalLeftRight()
						.setDividerLocation(MediatorGui.frame().getSplitHorizontalTopBottom()
								.getSplitVerticalLeftRight().getDividerLocation());
			} else {
				MediatorGui.frame().getSplitHorizontalTopBottom().getSplitVerticalLeftRight().setDividerLocation(
						MediatorGui.frame().getSplitHorizontalTopBottom().getSplitVerticalLeftRight().getWidth()
								- MediatorGui.frame().getSplitHorizontalTopBottom().getSplitVerticalLeftRight()
										.getDividerLocation());
			}
		}

		MediatorGui.tabResults().setComponentOrientation(ComponentOrientation.getOrientation(newLocale));

		// I18n of tree empty node
//        if (MediatorGui.treeDatabase().isRootVisible()) {
//            DefaultTreeModel model = (DefaultTreeModel) MediatorGui.managerDatabase().getTree().getModel();
//            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
//            model.reload(root);
//            MediatorGui.managerDatabase().getTree().revalidate();
//        }

		// Fix glitches on Linux
		MediatorGui.frame().revalidate();
	}

	// Getter and setter
	public JCheckBoxMenuItem getJavaDebugMenu() {
		return this.javaDebugMenu;
	}

	/**
	 * 递归转字体格式
	 * @param comp
	 * @param font
	 */
	private static void updateFont(Component comp, Font font) {
		comp.setFont(font);
		if (comp instanceof Container) {
			Container c = (Container) comp;
			int n = c.getComponentCount();
			for (int i = 0; i < n; i++) {
				updateFont(c.getComponent(i), font);
			}
		}
	}
	

}
