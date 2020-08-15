package com.jsql.view.swing.manager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jsql.i18n.I18n;
import com.jsql.model.MediatorModel;
import com.jsql.model.injection.method.MethodInjection;
import com.jsql.view.i18n.I18nView;
import com.jsql.view.swing.HelperUi;
import com.jsql.view.swing.MediatorGui;
import com.jsql.view.swing.list.BeanInjection;
import com.jsql.view.swing.list.DnDList;
import com.jsql.view.swing.list.ItemList;
import com.jsql.view.swing.list.ItemListScan;
import com.jsql.view.swing.list.ListTransfertHandlerScan;
import com.jsql.view.swing.manager.util.JButtonStateful;
import com.jsql.view.swing.manager.util.StateButton;
import com.jsql.view.swing.scrollpane.LightScrollPane;
import com.jsql.view.swing.ui.FlatButtonMouseAdapter;

/**
 * 	爬虫爬到的url管理页面
 */
@SuppressWarnings("serial")
public class ManagerScan extends AbstractManagerList {
    
    private static final Logger LOGGER = Logger.getRootLogger();
    
    public  List<ItemList> allItemsList = new ArrayList<>();
    
    final DnDList dndListScan;

    /**
     * 	在构造方法中传入待解析的json字符串
     */
    public ManagerScan(String jsonScan) {
        this.setLayout(new BorderLayout());

		/*
		 * StringBuilder jsonScan = new StringBuilder(); try { InputStream in =
		 * HelperUi.INPUT_STREAM_PAGES_SCAN; String line; BufferedReader reader = new
		 * BufferedReader(new InputStreamReader(in)); while ((line = reader.readLine())
		 * != null) { jsonScan.append(line); } reader.close(); } catch (IOException e) {
		 * LOGGER.error(e.getMessage(), e); }
		 */
        
        List<ItemList> itemsList = new ArrayList<>();
        
        JSONArray jsonArrayScan;
        try {
            jsonArrayScan = new JSONArray(jsonScan.toString());
            for (int i = 0 ; i < jsonArrayScan.length() ; i++) {
                JSONObject jsonObjectScan = jsonArrayScan.getJSONObject(i);
                BeanInjection beanInjection = new BeanInjection(
                    jsonObjectScan.getString("url"),
                    jsonObjectScan.optString("request"),
                    jsonObjectScan.optString("header"),
                    jsonObjectScan.optString("injectionType"),
                    jsonObjectScan.optString("vendor"),
                    jsonObjectScan.optString("requestType")
                );
                itemsList.add(new ItemListScan(beanInjection));
                allItemsList.add(new ItemListScan(beanInjection));
            }
        } catch (JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }
        
        dndListScan = new DnDList(itemsList);
        dndListScan.setName("scan");
        dndListScan.setTransferHandler(null);
        dndListScan.setTransferHandler(new ListTransfertHandlerScan());
        
        this.listPaths = dndListScan;
        this.getListPaths().setBorder(BorderFactory.createEmptyBorder(0, 0, LightScrollPane.THUMB_SIZE, 0));
        this.add(new LightScrollPane(1, 0, 0, 0, dndListScan), BorderLayout.CENTER);

        JPanel lastLine = new JPanel();
        lastLine.setOpaque(false);
        lastLine.setLayout(new BoxLayout(lastLine, BoxLayout.X_AXIS));

        lastLine.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 0, HelperUi.COLOR_COMPONENT_BORDER),
                BorderFactory.createEmptyBorder(1, 0, 1, 1)
            )
        );
        
        this.defaultText = "SCAN_RUN_BUTTON_LABEL";
        this.run = new JButtonStateful(this.defaultText);
        I18nView.addComponentForKey("SCAN_RUN_BUTTON_LABEL", this.run);
        this.run.setToolTipText(I18n.valueByKey("SCAN_RUN_BUTTON_TOOLTIP"));

        this.run.setContentAreaFilled(false);
        this.run.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        this.run.setBackground(new Color(200, 221, 242));
        
        this.run.addMouseListener(new FlatButtonMouseAdapter(this.run));
        
        this.run.addActionListener(actionEvent -> {
            if (dndListScan.getSelectedValuesList().isEmpty()) {
                LOGGER.warn("请选择要扫描的链接！");
                return;
            }
            new Thread(() -> {
                if (ManagerScan.this.run.getState() == StateButton.STARTABLE) {
                    ManagerScan.this.run.setText(I18nView.valueByKey("SCAN_RUN_BUTTON_STOP"));
                    ManagerScan.this.run.setState(StateButton.STOPPABLE);
                    ManagerScan.this.loader.setVisible(true);
                    
                    DefaultListModel<ItemList> listModel = (DefaultListModel<ItemList>) dndListScan.getModel();
                    for (int i = 0 ; i < listModel.getSize() ; i++) {
                        listModel.get(i).reset();
                    }
                    
                    MediatorModel.model().getResourceAccess().scanList(dndListScan.getSelectedValuesList());
                } else {
                    MediatorModel.model().getResourceAccess().setScanStopped(true);
                    MediatorModel.model().setIsStoppedByUser(true);
                    ManagerScan.this.run.setEnabled(false);
                    ManagerScan.this.run.setState(StateButton.STOPPING);
                }
            }, "ThreadScan").start();
            
        });

        this.loader.setVisible(false);

        lastLine.add(Box.createHorizontalGlue());
        lastLine.add(this.loader);
        lastLine.add(Box.createRigidArea(new Dimension(5, 0)));
        lastLine.add(this.run);
        
        this.add(lastLine, BorderLayout.SOUTH);
        
        dndListScan.addListSelectionListener(e -> {
            if (dndListScan.getSelectedValue() == null) {
                return;
            }
            BeanInjection beanInjection = ((ItemListScan) dndListScan.getSelectedValue()).getBeanInjection();
            
            MediatorGui.panelAddressBar().getTextFieldAddress().setText(beanInjection.getUrl());
            MediatorGui.panelAddressBar().getTextFieldHeader().setText(beanInjection.getHeader());
            MediatorGui.panelAddressBar().getTextFieldRequest().setText(beanInjection.getRequest());
            
            String requestType = beanInjection.getRequestType();
            if (requestType != null && !requestType.isEmpty()) {
                MediatorGui.panelAddressBar().getRadioRequest().setText(requestType);
//                Arrays.asList(new String[]{"OPTIONS", "HEAD", "POST", "PUT", "DELETE", "TRACE"}).contains(method)
            } else {
                MediatorGui.panelAddressBar().getRadioRequest().setText("POST");
            }
            
            MethodInjection injectionType = beanInjection.getInjectionTypeAsEnum();
            if (injectionType == MediatorModel.model().getMediatorMethodInjection().getHeader()) {
                MediatorGui.panelAddressBar().getRadioHeader().setSelected();
            } else if (injectionType == MediatorModel.model().getMediatorMethodInjection().getRequest()) {
                MediatorGui.panelAddressBar().getRadioRequest().setSelected();
            } else {
                MediatorGui.panelAddressBar().getRadioQueryString().setSelected();
            }
        });
    }
    
    /**
     * 
     * 	启动一个扫描线程
     */
    public void scanAll() {
    	new Thread(() -> {
            if (ManagerScan.this.run.getState() == StateButton.STARTABLE) {
                ManagerScan.this.run.setText(I18nView.valueByKey("SCAN_RUN_BUTTON_STOP"));
                ManagerScan.this.run.setState(StateButton.STOPPABLE);
                ManagerScan.this.loader.setVisible(true);
                
                DefaultListModel<ItemList> listModel = (DefaultListModel<ItemList>) dndListScan.getModel();
                int[] index = new int[listModel.getSize()];
                for (int i = 0 ; i < listModel.getSize() ; i++) {
                    listModel.get(i).reset();
                    index[i]=i;
                }
                dndListScan.setSelectedIndices(index);
                MediatorModel.model().getResourceAccess().scanList(dndListScan.getSelectedValuesList());
            } else {
                MediatorModel.model().getResourceAccess().setScanStopped(true);
                MediatorModel.model().setIsStoppedByUser(true);
                ManagerScan.this.run.setEnabled(false);
                ManagerScan.this.run.setState(StateButton.STOPPING);
            }
        }, "ThreadScan").start();
    }
    
}
