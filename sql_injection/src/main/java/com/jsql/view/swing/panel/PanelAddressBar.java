package com.jsql.view.swing.panel;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicRadioButtonMenuItemUI;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.jsql.i18n.I18n;
import com.jsql.mapper.UrlMapper;
import com.jsql.model.MediatorModel;
import com.jsql.model.bean.SubUrl;
import com.jsql.model.bean.Url;
import com.jsql.model.bean.util.CrawlUrl;
import com.jsql.model.bean.util.Interaction;
import com.jsql.model.bean.util.Request;
import com.jsql.model.injection.method.MethodInjection;
import com.jsql.model.injection.strategy.AbstractStrategy;
import com.jsql.model.injection.strategy.StrategyInjectionError;
import com.jsql.model.injection.vendor.model.Vendor;
import com.jsql.model.injection.vendor.model.yaml.Method;
import com.jsql.util.HttpUrlUtil;
import com.jsql.util.SpringContextUtils;
import com.jsql.util.crawl.LinkQueue;
import com.jsql.util.crawl.MyCrawler;
import com.jsql.view.i18n.I18nView;
import com.jsql.view.swing.HelperUi;
import com.jsql.view.swing.MediatorGui;
import com.jsql.view.swing.manager.util.ComboMenu;
import com.jsql.view.swing.manager.util.StateButton;
import com.jsql.view.swing.menubar.Menubar;
import com.jsql.view.swing.panel.util.ButtonAddressBar;
import com.jsql.view.swing.panel.util.RadioMenuItemIconCustom;
import com.jsql.view.swing.progressBar.ProgressbarThread;
import com.jsql.view.swing.radio.RadioLinkMethod;
import com.jsql.view.swing.text.JPopupTextField;
import com.jsql.view.swing.text.JTextFieldAddressBar;
import com.jsql.view.swing.text.JTextFieldPlaceholder;
import com.jsql.view.swing.text.JTextFieldWithIcon;
import com.jsql.view.swing.text.JToolTipI18n;
import com.jsql.view.swing.ui.ComponentBorder;

/**
 * 创建一个用于输入Url的文本域panel
 */
@SuppressWarnings("serial")
public class PanelAddressBar extends JPanel {

	UrlMapper urlMapper = SpringContextUtils.getBean(UrlMapper.class);

	/**
	 * 日志
	 */
	private static final Logger LOGGER = Logger.getRootLogger();

	/**
	 * GET 地址栏
	 */
	private JTextField textFieldAddress;

	/**
	 * POST 地址栏
	 */
	private JTextField textFieldRequest;

	/**
	 * HEADER.
	 */
	private JTextField textFieldHeader;

	/**
	 * 当前注入方式
	 */
	private MethodInjection methodInjection = MediatorModel.model().getMediatorMethodInjection().getQuery();

	private String typeRequest = "POST";

	/**
	 * 加载动画
	 */
	private JLabel loader = new JLabel(HelperUi.ICON_LOADER_GIF);

	/**
	 * 注入按钮
	 */
	private ButtonAddressBar buttonInUrl = new ButtonAddressBar();

	private boolean advanceIsActivated = true;

	private final RadioLinkMethod radioQueryString = new RadioLinkMethod("GET", true,
			MediatorModel.model().getMediatorMethodInjection().getQuery());
	private final RadioLinkMethod radioMethod = new RadioLinkMethod("POST",
			MediatorModel.model().getMediatorMethodInjection().getRequest());
	private final RadioLinkMethod radioHeader = new RadioLinkMethod("Header",
			MediatorModel.model().getMediatorMethodInjection().getHeader());

	private JMenu menuVendor;

	private JMenu[] itemRadioStrategyError = new JMenu[1];

	private ButtonGroup groupStrategy = new ButtonGroup();

	ProgressbarThread progressBarThread;

	public PanelAddressBar() {
		final JToolTipI18n[] j = new JToolTipI18n[] { new JToolTipI18n(I18n.valueByKey("FIELD_QUERYSTRING_TOOLTIP")) };
		JTextFieldWithIcon fieldWithIcon = new JTextFieldWithIcon(I18n.valueByKey("ADDRESS_BAR")) {
			@Override
			public JToolTip createToolTip() {
				JToolTip tipI18n = new JToolTipI18n(I18n.valueByKey("FIELD_QUERYSTRING_TOOLTIP"));
				j[0] = (JToolTipI18n) tipI18n;
				return tipI18n;
			}
		};
		this.textFieldAddress = new JTextFieldAddressBar(fieldWithIcon).getProxy();
		I18nView.addComponentForKey("ADDRESS_BAR", fieldWithIcon);
		I18nView.addComponentForKey("FIELD_QUERYSTRING_TOOLTIP", j[0]);

		final JToolTipI18n[] j2 = new JToolTipI18n[] { new JToolTipI18n(I18n.valueByKey("FIELD_REQUEST_TOOLTIP")) };
		this.textFieldRequest = new JPopupTextField(new JTextFieldPlaceholder("e.g. key=value&injectMe=") {
			@Override
			public JToolTip createToolTip() {
				JToolTip tipI18n = new JToolTipI18n(I18n.valueByKey("FIELD_REQUEST_TOOLTIP"));
				j2[0] = (JToolTipI18n) tipI18n;
				return tipI18n;
			}
		}).getProxy();
		I18nView.addComponentForKey("FIELD_REQUEST_TOOLTIP", j2[0]);

		final JToolTipI18n[] j3 = new JToolTipI18n[] { new JToolTipI18n(I18n.valueByKey("FIELD_HEADER_TOOLTIP")) };
		this.textFieldHeader = new JPopupTextField(new JTextFieldPlaceholder(
				"e.g. key: value\\r\\nCookie: cKey1=cValue1; cKey2=cValue2\\r\\nAuthorization: Basic dXNlcjpwYXNz\\r\\ninjectMe:") {
			@Override
			public JToolTip createToolTip() {
				JToolTip tipI18n = new JToolTipI18n(I18n.valueByKey("FIELD_HEADER_TOOLTIP"));
				j3[0] = (JToolTipI18n) tipI18n;
				return j3[0];
			}
		}).getProxy();
		I18nView.addComponentForKey("FIELD_HEADER_TOOLTIP", j3[0]);

		final JPanel panelHttpProtocol = new JPanel();
		panelHttpProtocol.setLayout(new BoxLayout(panelHttpProtocol, BoxLayout.LINE_AXIS));
		panelHttpProtocol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
		panelHttpProtocol.setBorder(null);

		JButton buttonRequestMethod = new BasicArrowButton(SwingConstants.SOUTH);
		buttonRequestMethod.setBorderPainted(false);
		buttonRequestMethod.setOpaque(false);

		panelHttpProtocol.add(buttonRequestMethod);
		panelHttpProtocol.add(this.radioMethod);

		final JPopupMenu popup = new JPopupMenu();
		final ButtonGroup buttonGroup = new ButtonGroup();

		for (String protocol : new String[] { "OPTIONS", "HEAD", "POST", "PUT", "DELETE", "TRACE" }) {
			final JMenuItem newMenuItem = new JRadioButtonMenuItem(protocol, "POST".equals(protocol));
			newMenuItem.addActionListener(actionEvent -> {
				PanelAddressBar.this.typeRequest = newMenuItem.getText();
				this.radioMethod.setText(PanelAddressBar.this.typeRequest);
			});
			popup.add(newMenuItem);
			buttonGroup.add(newMenuItem);
		}

		for (AbstractButton radioButton : Collections.list(buttonGroup.getElements())) {
			radioButton.setUI(new BasicRadioButtonMenuItemUI() {
				@Override
				protected void doClick(MenuSelectionManager msm) {
					this.menuItem.doClick(0);
				}
			});
		}

		JPanel panelCustomMethod = new JPanel(new BorderLayout());
		final JTextField inputCustomMethod = new JPopupTextField("CUSTOM").getProxy();

		final JRadioButton radioCustomMethod = new JRadioButton();
		radioCustomMethod.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
		radioCustomMethod.setIcon(new RadioMenuItemIconCustom());

		buttonGroup.add(radioCustomMethod);

		radioCustomMethod.addActionListener(actionEvent -> {
			if (!"".equals(inputCustomMethod.getText())) {
				PanelAddressBar.this.typeRequest = inputCustomMethod.getText();
				this.radioMethod.setText(PanelAddressBar.this.typeRequest);
			} else {
				LOGGER.warn("请定义请求方法");
			}
		});

		panelCustomMethod.add(radioCustomMethod, BorderLayout.LINE_START);
		panelCustomMethod.add(inputCustomMethod, BorderLayout.CENTER);
		popup.insert(panelCustomMethod, popup.getComponentCount());

		buttonRequestMethod.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				popup.applyComponentOrientation(ComponentOrientation.getOrientation(I18n.getLocaleDefault()));
				if (ComponentOrientation
						.getOrientation(I18n.getLocaleDefault()) == ComponentOrientation.RIGHT_TO_LEFT) {
					radioCustomMethod.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));
				} else {
					radioCustomMethod.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
				}

				popup.show(e.getComponent(),
						ComponentOrientation
								.getOrientation(I18n.getLocaleDefault()) == ComponentOrientation.RIGHT_TO_LEFT
										? e.getComponent().getX() - e.getComponent().getWidth() - popup.getWidth()
										: e.getComponent().getX(),
						e.getComponent().getY() + e.getComponent().getWidth());

				popup.setLocation(
						ComponentOrientation
								.getOrientation(I18n.getLocaleDefault()) == ComponentOrientation.RIGHT_TO_LEFT
										? e.getComponent().getLocationOnScreen().x + e.getComponent().getWidth()
												- popup.getWidth()
										: e.getComponent().getLocationOnScreen().x,
						e.getComponent().getLocationOnScreen().y + e.getComponent().getWidth());
			}
		});

		// 组件的垂直定位
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		// 顶部的第一个面板包含文本组件
		JPanel panelTextFields = new JPanel();
		GroupLayout layoutTextFields = new GroupLayout(panelTextFields);
		panelTextFields.setLayout(layoutTextFields);
		panelTextFields.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));
		this.add(panelTextFields);

		this.radioQueryString.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		panelHttpProtocol.setBorder(BorderFactory.createEmptyBorder(6, 3, 0, 3));
		this.radioHeader.setBorder(BorderFactory.createEmptyBorder(6, 3, 0, 3));

		// 设置提示工具
		this.textFieldAddress.setToolTipText(I18n.valueByKey("FIELD_QUERYSTRING_TOOLTIP"));
		this.textFieldRequest.setToolTipText(I18n.valueByKey("FIELD_REQUEST_TOOLTIP"));
		this.textFieldHeader.setToolTipText(I18n.valueByKey("FIELD_HEADER_TOOLTIP"));

		this.radioQueryString.setToolTipText(I18n.valueByKey("METHOD_QUERYSTRING_TOOLTIP"));
		this.radioMethod.setToolTipText(I18n.valueByKey("METHOD_REQUEST_TOOLTIP"));
		this.radioHeader.setToolTipText(I18n.valueByKey("METHOD_HEADER_TOOLTIP"));

		/**
		 * 定义地址栏的 UI
		 */
		this.textFieldAddress
				.setBorder(
						BorderFactory
								.createCompoundBorder(
										BorderFactory.createCompoundBorder(
												BorderFactory.createMatteBorder(4, 2, 3, 0,
														HelperUi.COLOR_DEFAULT_BACKGROUND),
												BorderFactory.createLineBorder(HelperUi.COLOR_BLU)),
										BorderFactory.createEmptyBorder(2, 23, 2, 23)));

		this.textFieldRequest.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 2, 0, 0, HelperUi.COLOR_DEFAULT_BACKGROUND), HelperUi.BORDER_BLU));
		this.textFieldHeader.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 2, 0, 0, HelperUi.COLOR_DEFAULT_BACKGROUND), HelperUi.BORDER_BLU));

		this.textFieldRequest.setPreferredSize(new Dimension(0, 27));
		this.textFieldRequest.setFont(HelperUi.FONT_SEGOE_BIG);
		this.textFieldHeader.setPreferredSize(new Dimension(0, 27));
		this.textFieldHeader.setFont(HelperUi.FONT_SEGOE_BIG);

		this.textFieldAddress.addActionListener(new ActionEnterAddressBar());
		this.textFieldRequest.addActionListener(new ActionEnterAddressBar());
		this.textFieldHeader.addActionListener(new ActionEnterAddressBar());

		this.buttonInUrl.setToolTipText("injection");
		this.buttonInUrl.addActionListener(new ActionStart());
		ComponentBorder buttonInTextfield = new ComponentBorder(this.buttonInUrl, 17, 0);
		buttonInTextfield.install(this.textFieldAddress);

		JMenuBar panelLineBottom = new JMenuBar();
		panelLineBottom.setOpaque(false);
		panelLineBottom.setBorder(null);

		this.itemRadioStrategyError = new JMenu[1];

		for (final AbstractStrategy strategy : MediatorModel.model().getMediatorStrategy().getStrategies()) {
			if (strategy != MediatorModel.model().getMediatorStrategy().getUndefined()) {
				MenuElement itemRadioStrategy;

				if (strategy == MediatorModel.model().getMediatorStrategy().getError()) {
					itemRadioStrategy = new JMenu(strategy.toString());
					this.itemRadioStrategyError[0] = (JMenu) itemRadioStrategy;
				} else {
					itemRadioStrategy = new JRadioButtonMenuItem(strategy.toString());
					((AbstractButton) itemRadioStrategy).addActionListener(actionEvent -> {
						// this.menuStrategy.setText(strategy.toString());
						MediatorModel.model().getMediatorStrategy().setStrategy(strategy);
					});
					this.groupStrategy.add((AbstractButton) itemRadioStrategy);
				}

				((JComponent) itemRadioStrategy)
						.setToolTipText(I18n.valueByKey("STRATEGY_" + strategy.getName().toUpperCase() + "_TOOLTIP"));
				((JComponent) itemRadioStrategy).setEnabled(false);
			}
		}

		// 数据库类型下拉框
		this.menuVendor = new ComboMenu(MediatorModel.model().getMediatorVendor().getAuto().toString());

		ButtonGroup groupVendor = new ButtonGroup();

		for (final Vendor vendor : MediatorModel.model().getMediatorVendor().getVendors()) {
			JMenuItem itemRadioVendor = new JRadioButtonMenuItem(vendor.toString(),
					vendor == MediatorModel.model().getMediatorVendor().getAuto());
			itemRadioVendor.addActionListener(actionEvent -> {
				this.menuVendor.setText(vendor.toString());
				MediatorModel.model().getMediatorVendor().setVendorByUser(vendor);
			});
			this.menuVendor.add(itemRadioVendor);
			groupVendor.add(itemRadioVendor);
		}

		panelLineBottom.add(Box.createHorizontalGlue());
		panelLineBottom.add(this.loader);
		panelLineBottom.add(Box.createHorizontalStrut(5));
		panelLineBottom.add(this.menuVendor);

		this.loader.setVisible(false);

		new ComponentBorder(panelLineBottom, 17, 0).install(this.textFieldAddress);
		this.loader.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

		final BasicArrowButton advancedButton = new BasicArrowButton(SwingConstants.NORTH);
		advancedButton.setBorderPainted(false);
		advancedButton.setOpaque(false);

		// 水平列规则
		layoutTextFields.setHorizontalGroup(layoutTextFields.createSequentialGroup()
				// 标签宽度固定
				.addGroup(layoutTextFields.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
						.addComponent(this.radioQueryString).addComponent(panelHttpProtocol)
						.addComponent(this.radioHeader)
				// 可调整大小的文本字段
				).addGroup(layoutTextFields.createParallelGroup().addComponent(this.textFieldAddress)
						.addComponent(this.textFieldRequest).addComponent(this.textFieldHeader)
				// 单选框宽度固定
				).addGroup(layoutTextFields.createParallelGroup(GroupLayout.Alignment.LEADING, false)
						.addComponent(advancedButton)));

		// 垂直线规则
		layoutTextFields.setVerticalGroup(layoutTextFields.createSequentialGroup()
				.addGroup(layoutTextFields.createParallelGroup(GroupLayout.Alignment.CENTER, false)
						.addComponent(this.radioQueryString).addComponent(this.textFieldAddress)
						.addComponent(advancedButton))
				.addGroup(layoutTextFields.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(panelHttpProtocol).addComponent(this.textFieldRequest))
				.addGroup(layoutTextFields.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(this.radioHeader).addComponent(this.textFieldHeader)));

		// 设置地址栏，标签等可见
		this.radioQueryString.setVisible(true);

		this.textFieldRequest.setVisible(true);
		panelHttpProtocol.setVisible(true);

		this.textFieldHeader.setVisible(true);
		this.radioHeader.setVisible(true);

		// advancedButton.setToolTipText(I18n.valueByKey("BUTTON_ADVANCED"));
		advancedButton.addActionListener(actionEvent -> {
			boolean isVisible = advancedButton.getDirection() == SwingConstants.SOUTH;

			this.radioQueryString.setVisible(isVisible);

			PanelAddressBar.this.textFieldRequest.setVisible(isVisible);
			panelHttpProtocol.setVisible(isVisible);

			PanelAddressBar.this.textFieldHeader.setVisible(isVisible);
			this.radioHeader.setVisible(isVisible);

			this.advanceIsActivated = isVisible;
			MediatorGui.menubar().setVisible(isVisible);

			advancedButton.setDirection(isVisible ? SwingConstants.NORTH : SwingConstants.SOUTH);
		});
	}

	private List<SubUrl> toSubUrlList(List<CrawlUrl> urlList, String parentUrlId) {
		SubUrl subUrl = null;
		List<SubUrl> subUrlList = new ArrayList<>();
		for (CrawlUrl crawlUrl : urlList) {
			subUrl = new SubUrl();
			subUrl.setUrlId(parentUrlId);
			subUrl.setCrawlUrl(crawlUrl.getUrl());
			subUrlList.add(subUrl);
		}
		return subUrlList;
	}

	/**
	 * 连接事件
	 */
	private class ActionStart implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {

			// 无注入检测时
			if (PanelAddressBar.this.getButtonInUrl().getState() == StateButton.STARTABLE) {
//               if(MyCrawler.STOP == false) {
//            	   LOGGER.info("正在爬取链接中，请在爬完或者终止后，再进行注入操作！");
//            	   return;
//               }
				// 清除先前注入中视图中的所有内容
				Request requests = new Request();
				requests.setMessage(Interaction.RESET_INTERFACE);
				MediatorModel.model().sendToViews(requests);
				this.startCrawl();
				// 当前正在运行注入检测，停止该进程
			} else if (PanelAddressBar.this.getButtonInUrl().getState() == StateButton.STOPPABLE) {
				if (MyCrawler.getStopStatus() == false) {
					Menubar.list = null;
					stopCrawl();
					this.stopInjection();
				} 
				progressBarThread.pauseThread();
			}
		}

		/**
		 * 开始爬链接
		 */
		protected void startCrawl() {
			MyCrawler.setStop(false);
			new Thread(new CrawlThread()).start();
		}

		/**
		 * 停止爬虫
		 */
		private void stopCrawl() {
			// 停止爬虫
			PanelAddressBar.this.getLoader().setVisible(false);
			MyCrawler.setStop(true);
			// PanelAddressBar.this.textFieldAddress.setText("");
			MediatorGui.tabManagers().refresh("[{\"url\":\"\"}]");
			LinkQueue.resetQueue();
		}

		/**
		 * 启动线程去爬取url
		 * 
		 * @author 12655
		 *
		 */
		class CrawlThread implements Runnable {
			public void run() {
				MyCrawler myCrawler = new MyCrawler();
				String url = PanelAddressBar.this.textFieldAddress.getText().trim();
				String urlId = UUID.randomUUID().toString();
				boolean isInsert = false;
				if (HttpUrlUtil.isHttpUrl(url)) {
					// 1.删除历史数据
					if(MediatorModel.model().isBatchScan() != true) {
						urlMapper.deleteUrl();
					}
					
					// 2.插入新数据
					Url urlBean = new Url();
					urlBean.setId(urlId);
					String accessVector = null;
					if (url.contains("localhost") || url.contains("LOCALHOST") || url.contains("127.0.0.1")) {
						accessVector = "LOCAL";
					} else {
						accessVector = "REMOTE";
					}
					urlBean.setAccessVector(accessVector);
					urlBean.setAccessComplexity("2");// 2代表高
					urlBean.setUrl(url);
					isInsert = urlMapper.insertUrl(urlBean) > 0 ? true : false;
					LOGGER.trace("开始爬取链接...");
					PanelAddressBar.this.getLoader().setVisible(true);//加载图标可见
					PanelAddressBar.this.getButtonInUrl().setToolTipText("stop checking");//设置提示
					PanelAddressBar.this.getButtonInUrl().setInjectionRunning();
					MediatorModel.model().setIsStoppedByUser(false);
					// 启一个进度条的进程
					progressBarThread = new ProgressbarThread(SplitHorizontalTopBottom.getProgressBar());
					progressBarThread.start();
					
					// 这里要启动线程去处理
					myCrawler.crawling(new String[] { url });
					List<CrawlUrl> urlList = new ArrayList<>();
					if (LinkQueue.isUnvisitedUrlsEmpty() || MyCrawler.getStopStatus() == true) {
						Set<Object> visitedUrlList = LinkQueue.getVisitedUrl();
						for (Object cUrl : visitedUrlList) {
							CrawlUrl crawUrl = new CrawlUrl();
							crawUrl.setUrl((String) cUrl);
							crawUrl.setRequestType("GET");
							crawUrl.setHeader(PanelAddressBar.this.textFieldHeader.getText().trim());
							crawUrl.setRequest(PanelAddressBar.this.textFieldRequest.getText().trim());
							crawUrl.setInjectionType(PanelAddressBar.this.methodInjection);
							urlList.add(crawUrl);
						}
						String urlJsonStr = "[{\"url\":\"\"}]";
						if (urlList.size() != 0) {
							urlJsonStr = JSON.toJSONString(urlList);
							if (isInsert) {
								urlMapper.updateAuthenticationStatus(urlId);
								urlMapper.batchInsertSubUrl(toSubUrlList(urlList, urlId));
							}
						}
						MediatorGui.tabManagers().refresh(urlJsonStr);
						MyCrawler.setStop(true);//控制爬虫进程结束
						LinkQueue.resetQueue();
						
						if(progressBarThread.getCountValue() < 49) {
							progressBarThread.pauseThread();
							progressBarThread.setCountValue(50);
							SplitHorizontalTopBottom.getProgressBar().setValue(50);
						}
						
						progressBarThread.resumeThread();
						// 开始注入检测
						startInjection();
					}

				} else {
					LOGGER.info("请输入正确的链接！");
				}
			}
		}

		protected void startInjection() {
			int option = 0;
			// 询问用户确认是否已经建立注入检测
			if (MediatorModel.model().isInjectionAlreadyBuilt()) {
				try {
					option = JOptionPane.showConfirmDialog(null, I18n.valueByKey("DIALOG_NEW_INJECTION_TEXT"),
							I18n.valueByKey("DIALOG_NEW_INJECTION_TITLE"), JOptionPane.OK_CANCEL_OPTION);
				} catch (ClassCastException e) {
					LOGGER.error(e, e);
				}
			}

			// 开始注入检测
			if (!MediatorModel.model().isInjectionAlreadyBuilt() || option == JOptionPane.OK_OPTION) {

				// 扫描并检测
				// List<ItemList> crawledUrlList =
				// MediatorGui.tabManagers().managerScanList.allItemsList;
				// MediatorModel.model().getResourceAccess().scanList(crawledUrlList);
				MediatorGui.managerScan().scanAll();

			}
		}

		private void stopInjection() {
			PanelAddressBar.this.getLoader().setVisible(false);
			PanelAddressBar.this.getButtonInUrl().setInjectionReady();
			PanelAddressBar.this.getButtonInUrl().setToolTipText(I18n.valueByKey("BUTTON_STOPPING_TOOLTIP"));
			MediatorModel.model().setIsStoppedByUser(true);
		}
	}

	private class ActionEnterAddressBar extends ActionStart {
		@Override
		public void actionPerformed(ActionEvent e) {
			// 当运行无注入时
			if (PanelAddressBar.this.getButtonInUrl().getState() == StateButton.STARTABLE) {
				//this.startInjection();
				MediatorGui.managerScan().scanAll();
			}
		}
	}

	public void initErrorMethods(Vendor vendor) {
		this.itemRadioStrategyError[0].removeAll();

		Integer[] i = { 0 };
		if (vendor != MediatorModel.model().getMediatorVendor().getAuto()
				&& vendor.instance().getModelYaml().getStrategy().getError() != null) {
			for (Method methodError : vendor.instance().getModelYaml().getStrategy().getError().getMethod()) {
				JMenuItem itemRadioVendor = new JRadioButtonMenuItem(methodError.getName());
				itemRadioVendor.setEnabled(false);
				this.itemRadioStrategyError[0].add(itemRadioVendor);
				this.groupStrategy.add(itemRadioVendor);

				final int indexError = i[0];
				itemRadioVendor.addActionListener(actionEvent -> {
					MediatorModel.model().getMediatorStrategy()
							.setStrategy(MediatorModel.model().getMediatorStrategy().getError());
					((StrategyInjectionError) MediatorModel.model().getMediatorStrategy().getError())
							.setIndexMethod(indexError);
				});

				i[0]++;
			}
		}
	}


	// Getter and setter
	
	public ProgressbarThread getProgressThread() {
		return this.progressBarThread;
	}


	public ButtonGroup getGroupStrategy() {
		return this.groupStrategy;
	}

	public JMenu getMenuVendor() {
		return this.menuVendor;
	}

	/**
	 * 根据选项的更改注入方法
	 */
	public void setMethodInjection(MethodInjection methodInjection) {
		this.methodInjection = methodInjection;
	}

	public JTextField getTextFieldAddress() {
		return this.textFieldAddress;
	}

	public JLabel getLoader() {
		return this.loader;
	}

	public ButtonAddressBar getButtonInUrl() {
		return this.buttonInUrl;
	}

	public boolean isAdvanceIsActivated() {
		return this.advanceIsActivated;
	}

	public JTextField getTextFieldRequest() {
		return this.textFieldRequest;
	}

	public JTextField getTextFieldHeader() {
		return this.textFieldHeader;
	}

	public String getTypeRequest() {
		return this.typeRequest;
	}

	public RadioLinkMethod getRadioQueryString() {
		return this.radioQueryString;
	}

	public RadioLinkMethod getRadioRequest() {
		return this.radioMethod;
	}

	public RadioLinkMethod getRadioHeader() {
		return this.radioHeader;
	}

}