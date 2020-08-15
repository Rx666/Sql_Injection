package com.jsql.view.swing.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentListener;

import org.apache.commons.text.WordUtils;

import com.jsql.model.MediatorModel;
import com.jsql.util.tampering.Tampering;
import com.jsql.view.swing.HelperUi;
import com.jsql.view.swing.MediatorGui;
import com.jsql.view.swing.action.ActionCheckIP;
import com.jsql.view.swing.action.ActionNewWindow;
import com.jsql.view.swing.panel.util.TamperingMouseAdapter;
import com.jsql.view.swing.scrollpane.LightScrollPane;
import com.jsql.view.swing.sql.lexer.HighlightedDocument;
import com.jsql.view.swing.text.JPopupTextField;
import com.jsql.view.swing.text.JPopupTextPane;
import com.jsql.view.swing.text.JTextPanePlaceholder;
import com.jsql.view.swing.text.listener.DocumentListenerTyping;
import com.jsql.view.swing.ui.FlatButtonMouseAdapter;

@SuppressWarnings("serial")
public class PanelPreferences extends JPanel {
    
    final JCheckBox checkboxIsTamperingBase64 = new JCheckBox();
    final JCheckBox checkboxIsTamperingVersionComment = new JCheckBox();
    final JCheckBox checkboxIsTamperingFunctionComment = new JCheckBox();
    final JCheckBox checkboxIsTamperingEqualToLike = new JCheckBox();
    final JCheckBox checkboxIsTamperingRandomCase = new JCheckBox();
    final JCheckBox checkboxIsTamperingEval = new JCheckBox();
    final JCheckBox checkboxIsTamperingHexToChar = new JCheckBox();
    final JCheckBox checkboxIsTamperingQuoteToUtf8 = new JCheckBox();
    final JRadioButton radioIsTamperingSpaceToMultilineComment = new JRadioButton();
    final JRadioButton radioIsTamperingSpaceToDashComment = new JRadioButton();
    final JRadioButton radioIsTamperingSpaceToSharpComment = new JRadioButton();
    
    final JCheckBox checkboxIsUsingProxy = new JCheckBox("", MediatorModel.model().getMediatorUtils().getProxyUtil().isUsingProxy());
    final JCheckBox checkboxIsUsingProxyHttps = new JCheckBox("", MediatorModel.model().getMediatorUtils().getProxyUtil().isUsingProxyHttps());
    
    final JCheckBox checkboxIsFollowingRedirection = new JCheckBox("", MediatorModel.model().getMediatorUtils().getPreferencesUtil().isFollowingRedirection());
    final JCheckBox checkboxIsNotInjectingMetadata = new JCheckBox("", MediatorModel.model().getMediatorUtils().getPreferencesUtil().isNotInjectingMetadata());
    final JCheckBox checkboxIsNotTestingConnection = new JCheckBox("", MediatorModel.model().getMediatorUtils().getPreferencesUtil().isNotTestingConnection());
    final JCheckBox checkboxIsParsingForm = new JCheckBox("", MediatorModel.model().getMediatorUtils().getPreferencesUtil().isParsingForm());
    
    final JCheckBox checkboxIsCheckingAllParam = new JCheckBox("", MediatorModel.model().getMediatorUtils().getPreferencesUtil().isCheckingAllParam());
    final JCheckBox checkboxIsCheckingAllURLParam = new JCheckBox("", MediatorModel.model().getMediatorUtils().getPreferencesUtil().isCheckingAllURLParam());
    final JCheckBox checkboxIsCheckingAllRequestParam = new JCheckBox("", MediatorModel.model().getMediatorUtils().getPreferencesUtil().isCheckingAllRequestParam());
    final JCheckBox checkboxIsCheckingAllHeaderParam = new JCheckBox("", MediatorModel.model().getMediatorUtils().getPreferencesUtil().isCheckingAllHeaderParam());
    final JCheckBox checkboxIsCheckingAllJSONParam = new JCheckBox("", MediatorModel.model().getMediatorUtils().getPreferencesUtil().isCheckingAllJSONParam());
    final JCheckBox checkboxIsCheckingAllCookieParam = new JCheckBox("", MediatorModel.model().getMediatorUtils().getPreferencesUtil().isCheckingAllCookieParam());
    final JCheckBox checkboxIsCheckingAllSOAPParam = new JCheckBox("", MediatorModel.model().getMediatorUtils().getPreferencesUtil().isCheckingAllSOAPParam());

    final JCheckBox checkboxProcessCookies = new JCheckBox("", MediatorModel.model().getMediatorUtils().getPreferencesUtil().isProcessingCookies());
    final JCheckBox checkboxProcessCsrf = new JCheckBox("", MediatorModel.model().getMediatorUtils().getPreferencesUtil().isProcessingCsrf());

    final JTextField textProxyAddress = new JPopupTextField("e.g Tor address: 127.0.0.1", MediatorModel.model().getMediatorUtils().getProxyUtil().getProxyAddress()).getProxy();
    final JTextField textProxyPort = new JPopupTextField("e.g Tor port: 8118", MediatorModel.model().getMediatorUtils().getProxyUtil().getProxyPort()).getProxy();
    final JTextField textProxyAddressHttps = new JPopupTextField("e.g Tor address: 127.0.0.1", MediatorModel.model().getMediatorUtils().getProxyUtil().getProxyAddressHttps()).getProxy();
    final JTextField textProxyPortHttps = new JPopupTextField("e.g Tor port: 8118", MediatorModel.model().getMediatorUtils().getProxyUtil().getProxyPortHttps()).getProxy();

    final JCheckBox checkboxUseDigestAuthentication = new JCheckBox("", MediatorModel.model().getMediatorUtils().getAuthenticationUtil().isDigestAuthentication());
    final JCheckBox checkboxUseKerberos = new JCheckBox("", MediatorModel.model().getMediatorUtils().getAuthenticationUtil().isKerberos());

    final JTextField textDigestAuthenticationUsername = new JPopupTextField("Host system user", MediatorModel.model().getMediatorUtils().getAuthenticationUtil().getUsernameDigest()).getProxy();
    final JTextField textDigestAuthenticationPassword = new JPopupTextField("Host system password", MediatorModel.model().getMediatorUtils().getAuthenticationUtil().getPasswordDigest()).getProxy();
    final JTextField textKerberosLoginConf = new JPopupTextField("Path to login.conf", MediatorModel.model().getMediatorUtils().getAuthenticationUtil().getPathKerberosLogin()).getProxy();
    final JTextField textKerberosKrb5Conf = new JPopupTextField("Path to krb5.conf", MediatorModel.model().getMediatorUtils().getAuthenticationUtil().getPathKerberosKrb5()).getProxy();

    private class ActionListenerSave implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            MediatorModel.model().getMediatorUtils().getPreferencesUtil().set(
                PanelPreferences.this.checkboxIsFollowingRedirection.isSelected(),
                PanelPreferences.this.checkboxIsNotInjectingMetadata.isSelected(),
                
                PanelPreferences.this.checkboxIsCheckingAllParam.isSelected(),
                PanelPreferences.this.checkboxIsCheckingAllURLParam.isSelected(),
                PanelPreferences.this.checkboxIsCheckingAllRequestParam.isSelected(),
                PanelPreferences.this.checkboxIsCheckingAllHeaderParam.isSelected(),
                PanelPreferences.this.checkboxIsCheckingAllJSONParam.isSelected(),
                PanelPreferences.this.checkboxIsCheckingAllCookieParam.isSelected(),
                PanelPreferences.this.checkboxIsCheckingAllSOAPParam.isSelected(),
                
                PanelPreferences.this.checkboxIsParsingForm.isSelected(),
                PanelPreferences.this.checkboxIsNotTestingConnection.isSelected(),
                PanelPreferences.this.checkboxProcessCookies.isSelected(),
                PanelPreferences.this.checkboxProcessCsrf.isSelected(),
                
                PanelPreferences.this.checkboxIsTamperingBase64.isSelected(),
                PanelPreferences.this.checkboxIsTamperingEqualToLike.isSelected(),
                PanelPreferences.this.checkboxIsTamperingFunctionComment.isSelected(),
                PanelPreferences.this.checkboxIsTamperingVersionComment.isSelected(),
                PanelPreferences.this.checkboxIsTamperingRandomCase.isSelected(),
                PanelPreferences.this.checkboxIsTamperingEval.isSelected(),
                PanelPreferences.this.radioIsTamperingSpaceToDashComment.isSelected(),
                PanelPreferences.this.radioIsTamperingSpaceToMultilineComment.isSelected(),
                PanelPreferences.this.radioIsTamperingSpaceToSharpComment.isSelected()
                
            );
            
            MediatorModel.model().getMediatorUtils().getProxyUtil().set(
                PanelPreferences.this.checkboxIsUsingProxy.isSelected(),
                PanelPreferences.this.textProxyAddress.getText(),
                PanelPreferences.this.textProxyPort.getText(),
                PanelPreferences.this.checkboxIsUsingProxyHttps.isSelected(),
                PanelPreferences.this.textProxyAddressHttps.getText(),
                PanelPreferences.this.textProxyPortHttps.getText()
            );
            
            MediatorModel.model().getMediatorUtils().getTamperingUtil().set(
                PanelPreferences.this.checkboxIsTamperingBase64.isSelected(),
                PanelPreferences.this.checkboxIsTamperingVersionComment.isSelected(),
                PanelPreferences.this.checkboxIsTamperingFunctionComment.isSelected(),
                PanelPreferences.this.checkboxIsTamperingEqualToLike.isSelected(),
                PanelPreferences.this.checkboxIsTamperingRandomCase.isSelected(),
                PanelPreferences.this.checkboxIsTamperingHexToChar.isSelected(),
                PanelPreferences.this.checkboxIsTamperingQuoteToUtf8.isSelected(),
                PanelPreferences.this.checkboxIsTamperingEval.isSelected(),
                PanelPreferences.this.radioIsTamperingSpaceToMultilineComment.isSelected(),
                PanelPreferences.this.radioIsTamperingSpaceToDashComment.isSelected(),
                PanelPreferences.this.radioIsTamperingSpaceToSharpComment.isSelected()
            );
            
            boolean isRestartRequired = MediatorModel.model().getMediatorUtils().getAuthenticationUtil().set(
                PanelPreferences.this.checkboxUseDigestAuthentication.isSelected(),
                PanelPreferences.this.textDigestAuthenticationUsername.getText(),
                PanelPreferences.this.textDigestAuthenticationPassword.getText(),
                PanelPreferences.this.checkboxUseKerberos.isSelected(),
                PanelPreferences.this.textKerberosKrb5Conf.getText(),
                PanelPreferences.this.textKerberosLoginConf.getText()
            );
            
            if (
                isRestartRequired
                && JOptionPane.showConfirmDialog(
                    MediatorGui.frame(),
                        "File krb5.conf has changed, please restart.",
                        "Restart",
                        JOptionPane.YES_NO_OPTION
                    ) == JOptionPane.YES_OPTION
            ) {
                new ActionNewWindow().actionPerformed(null);
            }
        }
        
    }
    
    private transient ActionListener actionListenerSave = new ActionListenerSave();

    private static final JPanel panelInjection = new JPanel(new BorderLayout());
    private static final JPanel panelAuthentication = new JPanel(new BorderLayout());
    private static final JPanel panelProxy = new JPanel(new BorderLayout());
    private static final JPanel panelGeneral = new JPanel(new BorderLayout());
    private static final JPanel panelTampering = new JPanel(new BorderLayout());
    
    private enum CategoryPreference {
        
        INJECTION(panelInjection),
        TAMPERING(panelTampering),
        PROXY(panelProxy),
        AUTHENTICATION(panelAuthentication),
        GENERAL(panelGeneral);
//        USER_AGENT(new JPanel());
        
        private Component panel;

        private CategoryPreference(Component panel) {
            this.panel = panel;
        }
        
        @Override
        public String toString() {
            return "  "+ WordUtils.capitalizeFully(this.name()).replace('_', ' ') +"  ";
        }

        public Component getPanel() {
            return this.panel;
        }
        
    }
    
    public PanelPreferences() {
        BorderLayout borderLayoutPreferences = new BorderLayout();
        this.setLayout(borderLayoutPreferences);

        final JButton buttonCheckIp = new JButton("Check your IP");
        buttonCheckIp.addActionListener(new ActionCheckIP());
        buttonCheckIp.setToolTipText(
            "<html><b>Verify what public IP address is used by jSQL</b><br>"
            + "Usually it's your own public IP if you don't use a proxy. If you use a proxy<br>"
            + "like TOR then your public IP is hidden and another one is used instead.</html>"
        );
        buttonCheckIp.setContentAreaFilled(true);
        buttonCheckIp.setBorder(HelperUi.BORDER_ROUND_BLU);
        
        FlatButtonMouseAdapter flatButtonMouseAdapter = new FlatButtonMouseAdapter(buttonCheckIp);
        flatButtonMouseAdapter.setContentVisible(true);
        buttonCheckIp.addMouseListener(flatButtonMouseAdapter);

        /**/
        String tooltipIsTamperingBase64 = Tampering.BASE64.instance().getModelYaml().getTooltip();
        this.checkboxIsTamperingBase64.setToolTipText(tooltipIsTamperingBase64);
        this.checkboxIsTamperingBase64.setFocusable(false);
        JButton labelIsTamperingBase64 = new JButton(Tampering.BASE64.instance().getModelYaml().getDescription());
        labelIsTamperingBase64.setToolTipText(tooltipIsTamperingBase64);
        labelIsTamperingBase64.addActionListener(actionEvent -> {
            this.checkboxIsTamperingBase64.setSelected(!this.checkboxIsTamperingBase64.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        
        String tooltipIsTamperingFunctionComment = Tampering.COMMENT_TO_METHOD_SIGNATURE.instance().getModelYaml().getTooltip();
        this.checkboxIsTamperingFunctionComment.setToolTipText(tooltipIsTamperingFunctionComment);
        this.checkboxIsTamperingFunctionComment.setFocusable(false);
        JButton labelIsTamperingFunctionComment = new JButton(Tampering.COMMENT_TO_METHOD_SIGNATURE.instance().getModelYaml().getDescription());
        labelIsTamperingFunctionComment.setToolTipText(tooltipIsTamperingFunctionComment);
        labelIsTamperingFunctionComment.addActionListener(actionEvent -> {
            this.checkboxIsTamperingFunctionComment.setSelected(!this.checkboxIsTamperingFunctionComment.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        
        String tooltipIsTamperingEqualToLike = Tampering.EQUAL_TO_LIKE.instance().getModelYaml().getTooltip();
        this.checkboxIsTamperingEqualToLike.setToolTipText(tooltipIsTamperingEqualToLike);
        this.checkboxIsTamperingEqualToLike.setFocusable(false);
        JButton labelIsTamperingEqualToLike = new JButton(Tampering.EQUAL_TO_LIKE.instance().getModelYaml().getDescription());
        labelIsTamperingEqualToLike.setToolTipText(tooltipIsTamperingEqualToLike);
        labelIsTamperingEqualToLike.addActionListener(actionEvent -> {
            this.checkboxIsTamperingEqualToLike.setSelected(!this.checkboxIsTamperingEqualToLike.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        
        String tooltipIsTamperingRandomCase = Tampering.RANDOM_CASE.instance().getModelYaml().getTooltip();
        this.checkboxIsTamperingRandomCase.setToolTipText(tooltipIsTamperingRandomCase);
        this.checkboxIsTamperingRandomCase.setFocusable(false);
        JButton labelIsTamperingRandomCase = new JButton(Tampering.RANDOM_CASE.instance().getModelYaml().getDescription());
        labelIsTamperingRandomCase.setToolTipText(tooltipIsTamperingRandomCase);
        labelIsTamperingRandomCase.addActionListener(actionEvent -> {
            this.checkboxIsTamperingRandomCase.setSelected(!this.checkboxIsTamperingRandomCase.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        
        String tooltipIsTamperingEval = "Custom tamper in JavaScript, e.g sql.replace(/\\+/gm,'/**/')";
        this.checkboxIsTamperingEval.setToolTipText(tooltipIsTamperingEval);
        this.checkboxIsTamperingEval.setFocusable(false);
        JTextPane l = new JPopupTextPane(new JTextPanePlaceholder(tooltipIsTamperingEval)).getProxy();
        LightScrollPane textAreaIsTamperingEval = new LightScrollPane(l);
        textAreaIsTamperingEval.setBorder(HelperUi.BORDER_FOCUS_LOST);
        textAreaIsTamperingEval.setMinimumSize(new Dimension(400, 100));
        
        ButtonGroup n = new ButtonGroup();
        n.add(this.radioIsTamperingSpaceToDashComment);
        n.add(this.radioIsTamperingSpaceToMultilineComment);
        n.add(this.radioIsTamperingSpaceToSharpComment);
        
        String tooltipIsTamperingSpaceToMultilineComment = Tampering.SPACE_TO_MULTILINE_COMMENT.instance().getModelYaml().getTooltip();
        this.radioIsTamperingSpaceToMultilineComment.setToolTipText(tooltipIsTamperingSpaceToMultilineComment);
        this.radioIsTamperingSpaceToMultilineComment.setFocusable(false);
        JButton labelIsTamperingSpaceToMultilineComment = new JButton(Tampering.SPACE_TO_MULTILINE_COMMENT.instance().getModelYaml().getDescription());
        labelIsTamperingSpaceToMultilineComment.setToolTipText(tooltipIsTamperingSpaceToMultilineComment);
        labelIsTamperingSpaceToMultilineComment.addActionListener(actionEvent -> {
            if (this.radioIsTamperingSpaceToMultilineComment.isSelected()) {
                n.clearSelection();
            } else {
                this.radioIsTamperingSpaceToMultilineComment.setSelected(!this.radioIsTamperingSpaceToMultilineComment.isSelected());
            }
            this.actionListenerSave.actionPerformed(null);
        });
        
        String tooltipIsTamperingSpaceToDashComment = Tampering.SPACE_TO_DASH_COMMENT.instance().getModelYaml().getTooltip();
        this.radioIsTamperingSpaceToDashComment.setToolTipText(tooltipIsTamperingSpaceToDashComment);
        this.radioIsTamperingSpaceToDashComment.setFocusable(false);
        JButton labelIsTamperingSpaceToDashComment = new JButton(Tampering.SPACE_TO_DASH_COMMENT.instance().getModelYaml().getDescription());
        labelIsTamperingSpaceToDashComment.setToolTipText(tooltipIsTamperingSpaceToDashComment);
        labelIsTamperingSpaceToDashComment.addActionListener(actionEvent -> {
            if (this.radioIsTamperingSpaceToDashComment.isSelected()) {
                n.clearSelection();
            } else {
                this.radioIsTamperingSpaceToDashComment.setSelected(!this.radioIsTamperingSpaceToDashComment.isSelected());
            }
            this.actionListenerSave.actionPerformed(null);
        });
        
        String tooltipIsTamperingSpaceToSharpComment = Tampering.SPACE_TO_SHARP_COMMENT.instance().getModelYaml().getTooltip();
        this.radioIsTamperingSpaceToSharpComment.setToolTipText(tooltipIsTamperingSpaceToSharpComment);
        this.radioIsTamperingSpaceToSharpComment.setFocusable(false);
        JButton labelIsTamperingSpaceToSharpComment = new JButton(Tampering.SPACE_TO_SHARP_COMMENT.instance().getModelYaml().getDescription());
        labelIsTamperingSpaceToSharpComment.setToolTipText(tooltipIsTamperingSpaceToSharpComment);
        labelIsTamperingSpaceToSharpComment.addActionListener(actionEvent -> {
            if (this.radioIsTamperingSpaceToSharpComment.isSelected()) {
                n.clearSelection();
            } else {
                this.radioIsTamperingSpaceToSharpComment.setSelected(!this.radioIsTamperingSpaceToSharpComment.isSelected());
            }
            this.actionListenerSave.actionPerformed(null);
        });
        /**/
        
        String tooltipIsTamperingVersionComment = Tampering.VERSIONED_COMMENT_TO_METHOD_SIGNATURE.instance().getModelYaml().getTooltip();
        this.checkboxIsTamperingVersionComment.setToolTipText(tooltipIsTamperingVersionComment);
        this.checkboxIsTamperingVersionComment.setFocusable(false);
        JButton labelIsTamperingVersionComment = new JButton(Tampering.VERSIONED_COMMENT_TO_METHOD_SIGNATURE.instance().getModelYaml().getDescription());
        labelIsTamperingVersionComment.setToolTipText(tooltipIsTamperingVersionComment);
        labelIsTamperingVersionComment.addActionListener(actionEvent -> {
            this.checkboxIsTamperingVersionComment.setSelected(!this.checkboxIsTamperingVersionComment.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        
        String tooltipIsTamperingHexToChar = Tampering.HEX_TO_CHAR.instance().getModelYaml().getTooltip();
        this.checkboxIsTamperingHexToChar.setToolTipText(tooltipIsTamperingHexToChar);
        this.checkboxIsTamperingHexToChar.setFocusable(false);
        JButton labelIsTamperingHexToChar = new JButton(Tampering.HEX_TO_CHAR.instance().getModelYaml().getDescription());
        labelIsTamperingHexToChar.setToolTipText(tooltipIsTamperingHexToChar);
        labelIsTamperingHexToChar.addActionListener(actionEvent -> {
            this.checkboxIsTamperingHexToChar.setSelected(!this.checkboxIsTamperingHexToChar.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        
        String tooltipIsTamperingQouteToUtf8 = Tampering.QUOTE_TO_UTF8.instance().getModelYaml().getTooltip();
        this.checkboxIsTamperingQuoteToUtf8.setToolTipText(tooltipIsTamperingQouteToUtf8);
        this.checkboxIsTamperingQuoteToUtf8.setFocusable(false);
        JButton labelIsTamperingQuoteToUtf8 = new JButton(Tampering.QUOTE_TO_UTF8.instance().getModelYaml().getDescription());
        labelIsTamperingQuoteToUtf8.setToolTipText(tooltipIsTamperingQouteToUtf8);
        labelIsTamperingQuoteToUtf8.addActionListener(actionEvent -> {
            this.checkboxIsTamperingQuoteToUtf8.setSelected(!this.checkboxIsTamperingQuoteToUtf8.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });

        labelIsTamperingSpaceToSharpComment.addMouseListener(new TamperingMouseAdapter(Tampering.SPACE_TO_SHARP_COMMENT, l));
        labelIsTamperingSpaceToDashComment.addMouseListener(new TamperingMouseAdapter(Tampering.SPACE_TO_DASH_COMMENT, l));
        labelIsTamperingBase64.addMouseListener(new TamperingMouseAdapter(Tampering.BASE64, l));
        labelIsTamperingEqualToLike.addMouseListener(new TamperingMouseAdapter(Tampering.EQUAL_TO_LIKE, l));
        labelIsTamperingFunctionComment.addMouseListener(new TamperingMouseAdapter(Tampering.COMMENT_TO_METHOD_SIGNATURE, l));
        labelIsTamperingRandomCase.addMouseListener(new TamperingMouseAdapter(Tampering.RANDOM_CASE, l));
        labelIsTamperingHexToChar.addMouseListener(new TamperingMouseAdapter(Tampering.HEX_TO_CHAR, l));
        labelIsTamperingQuoteToUtf8.addMouseListener(new TamperingMouseAdapter(Tampering.QUOTE_TO_UTF8, l));
        labelIsTamperingSpaceToMultilineComment.addMouseListener(new TamperingMouseAdapter(Tampering.SPACE_TO_MULTILINE_COMMENT, l));
        labelIsTamperingVersionComment.addMouseListener(new TamperingMouseAdapter(Tampering.VERSIONED_COMMENT_TO_METHOD_SIGNATURE, l));
        
        if (l.getStyledDocument() instanceof HighlightedDocument) {
            HighlightedDocument oldDocument = (HighlightedDocument) l.getStyledDocument();
            oldDocument.stopColorer();
        }
        
        HighlightedDocument document = new HighlightedDocument(HighlightedDocument.JAVASCRIPT_STYLE);
        document.setHighlightStyle(HighlightedDocument.JAVASCRIPT_STYLE);
        l.setStyledDocument(document);
        
        document.addDocumentListener(new DocumentListenerTyping() {
            
            @Override
            public void warn() {
                MediatorModel.model().getMediatorUtils().getTamperingUtil().setCustomTamper(l.getText());
            }
        });
        
        l.setText(MediatorModel.model().getMediatorUtils().getTamperingUtil().getCustomTamper());
        
        
        JButton labelIsCheckingUpdate = new JButton("Check update at startup");
        labelIsCheckingUpdate.addActionListener(actionEvent -> {
            this.actionListenerSave.actionPerformed(null);
        });
        
        String tooltipIsReportingBugs = "Send unhandled exception to developer in order to fix issues.";
        JButton labelIsReportingBugs = new JButton("Report unhandled exceptions");
        labelIsReportingBugs.setToolTipText(tooltipIsReportingBugs);
        labelIsReportingBugs.addActionListener(actionEvent -> {
            this.actionListenerSave.actionPerformed(null);
        });
        
        String tooltipIs4K = "Upscale GUI by factor 2.5 for compatibility with high-definition screens";

        JButton labelIs4K = new JButton("Activate high-definition mode for 4K screens (need a restart)");
        labelIs4K.setToolTipText(tooltipIs4K);
        labelIs4K.addActionListener(actionEvent -> {
            this.actionListenerSave.actionPerformed(null);
        });
        
        String tooltipIsFollowingRedirection = "Force redirection when the page has moved (e.g. HTTP/1.1 302 Found).";
        this.checkboxIsFollowingRedirection.setToolTipText(tooltipIsFollowingRedirection);
        this.checkboxIsFollowingRedirection.setFocusable(false);
        JButton labelIsFollowingRedirection = new JButton("Follow HTTP redirection");
        labelIsFollowingRedirection.setToolTipText(tooltipIsFollowingRedirection);
        labelIsFollowingRedirection.addActionListener(actionEvent -> {
            this.checkboxIsFollowingRedirection.setSelected(!this.checkboxIsFollowingRedirection.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        
        String tooltipTestConnection = "Disable initial connection test";
        this.checkboxIsNotTestingConnection.setToolTipText(tooltipTestConnection);
        this.checkboxIsNotTestingConnection.setFocusable(false);
        JButton labelTestConnection = new JButton("Disable initial connection test");
        labelTestConnection.setToolTipText(tooltipTestConnection);
        labelTestConnection.addActionListener(actionEvent -> {
            this.checkboxIsNotTestingConnection.setSelected(!this.checkboxIsNotTestingConnection.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        
        String tooltipProcessCookies = "Save session cookies";
        this.checkboxProcessCookies.setToolTipText(tooltipProcessCookies);
        this.checkboxProcessCookies.setFocusable(false);
        JButton labelProcessCookies = new JButton("Save session cookies");
        labelProcessCookies.setToolTipText(tooltipProcessCookies);
        labelProcessCookies.addActionListener(actionEvent -> {
            this.checkboxProcessCookies.setSelected(!this.checkboxProcessCookies.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        
        ActionListener actionListenerProcessCsrf = actionEvent -> {
            if (actionEvent.getSource() != this.checkboxProcessCsrf) {
                this.checkboxProcessCsrf.setSelected(!this.checkboxProcessCsrf.isSelected());
            }
            
            if (this.checkboxProcessCsrf.isSelected()) {
                this.checkboxProcessCookies.setSelected(this.checkboxProcessCsrf.isSelected());
            }
            this.checkboxProcessCookies.setEnabled(!this.checkboxProcessCsrf.isSelected());
            labelProcessCookies.setEnabled(!this.checkboxProcessCsrf.isSelected());
            
            this.actionListenerSave.actionPerformed(null);
        };
        
        this.checkboxProcessCookies.setEnabled(!this.checkboxProcessCsrf.isSelected());
        labelProcessCookies.setEnabled(!this.checkboxProcessCsrf.isSelected());
        
        String tooltipProcessCsrf = "Process CSRF token";
        this.checkboxProcessCsrf.setToolTipText(tooltipProcessCsrf);
        this.checkboxProcessCsrf.setFocusable(false);
        JButton labelProcessCsrf = new JButton("Process CSRF token");
        labelProcessCsrf.setToolTipText(tooltipProcessCsrf);
        labelProcessCsrf.addActionListener(actionListenerProcessCsrf);
        this.checkboxProcessCsrf.addActionListener(actionListenerProcessCsrf);
        
        String tooltipParseForm = "Add <input> parameters found in HTML body to URL and Request";
        this.checkboxIsParsingForm.setToolTipText(tooltipParseForm);
        this.checkboxIsParsingForm.setFocusable(false);
        JButton labelParseForm = new JButton("Add <input> parameters found in HTML body to URL and Request");
        labelParseForm.setToolTipText(tooltipParseForm);
        labelParseForm.addActionListener(actionEvent -> {
            this.checkboxIsParsingForm.setSelected(!this.checkboxIsParsingForm.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        
        String tooltipIsNotInjectingMetadata = "Disable database's metadata injection (e.g version, username).";
        this.checkboxIsNotInjectingMetadata.setToolTipText(tooltipIsNotInjectingMetadata);
        this.checkboxIsNotInjectingMetadata.setFocusable(false);
        JButton labelIsNotInjectingMetadata = new JButton("Disable database's metadata injection to speed-up boolean process");
        labelIsNotInjectingMetadata.setToolTipText(tooltipIsNotInjectingMetadata);
        labelIsNotInjectingMetadata.addActionListener(actionEvent -> {
            this.checkboxIsNotInjectingMetadata.setSelected(!this.checkboxIsNotInjectingMetadata.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        
        JPanel panelTamperingPreferences = new JPanel();
        panelTamperingPreferences.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panelTampering.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
        panelTampering.add(new JLabel("<html><b>Tampering</b> / SQL expression alteration to bypass Web Application Firewall</html>"), BorderLayout.NORTH);
        panelTampering.add(panelTamperingPreferences, BorderLayout.CENTER);
        
        JPanel panelGeneralPreferences = new JPanel();
        panelGeneralPreferences.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panelGeneral.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
        panelGeneral.add(new JLabel("<html><b>General</b> / Standard options</html>"), BorderLayout.NORTH);
        panelGeneral.add(panelGeneralPreferences, BorderLayout.CENTER);
        
        JPanel panelInjectionPreferences = new JPanel();
        panelInjectionPreferences.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panelInjection.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
        panelInjection.add(new JLabel("<html><b>Injection</b> / Algorithm configuration</html>"), BorderLayout.NORTH);
        panelInjection.add(panelInjectionPreferences, BorderLayout.CENTER);
        
        JPanel panelAuthenticationPreferences = new JPanel();
        panelAuthenticationPreferences.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panelAuthentication.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
        panelAuthentication.add(new JLabel("<html><b>Authentication</b> / Basic, Digest, NTLM or Kerberos</html>"), BorderLayout.NORTH);
        panelAuthentication.add(panelAuthenticationPreferences, BorderLayout.CENTER);
        
        JPanel panelProxyPreferences = new JPanel();
        panelProxyPreferences.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panelProxy.setLayout(new BoxLayout(panelProxy, BoxLayout.Y_AXIS));
        panelProxy.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
        
        JLabel labelProxy = new JLabel("<html><b>Proxy</b> / Define proxy settings (e.g. TOR)</html>");
        JLabel labelProxyHttpHidden = new JLabel();
        JLabel labelProxyHttp = new JLabel("<html><b>Handling proxy for HTTP protocol</b></html>");
        JLabel labelProxyHttpsHidden = new JLabel();
        JLabel labelProxyHttps = new JLabel("<html><b>Handling proxy for HTTPS protocol</b></html>");
        labelProxyHttp.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        labelProxyHttps.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        panelProxy.removeAll();
        panelProxy.add(labelProxy, BorderLayout.NORTH);
        panelProxy.add(panelProxyPreferences);
        
        JPanel panelCheckIp = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        panelCheckIp.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        panelCheckIp.add(buttonCheckIp);
        panelCheckIp.add(Box.createGlue());
        panelProxy.add(panelCheckIp);
        
        labelProxy.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelProxyPreferences.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelCheckIp.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        GroupLayout groupLayoutTampering = new GroupLayout(panelTamperingPreferences);
        panelTamperingPreferences.setLayout(groupLayoutTampering);
        GroupLayout groupLayoutGeneral = new GroupLayout(panelGeneralPreferences);
        panelGeneralPreferences.setLayout(groupLayoutGeneral);
        GroupLayout groupLayoutInjection = new GroupLayout(panelInjectionPreferences);
        panelInjectionPreferences.setLayout(groupLayoutInjection);
        GroupLayout groupLayoutAuthentication = new GroupLayout(panelAuthenticationPreferences);
        panelAuthenticationPreferences.setLayout(groupLayoutAuthentication);
        GroupLayout groupLayoutProxy = new GroupLayout(panelProxyPreferences);
        panelProxyPreferences.setLayout(groupLayoutProxy);

        JButton labelIsCheckingAllParam = new JButton("Inject each parameter and ignore user's method");
        JButton labelIsCheckingAllURLParam = new JButton("Inject each URL parameter if method is GET");
        JButton labelIsCheckingAllRequestParam = new JButton("Inject each Request parameter if method is Request");
        JButton labelIsCheckingAllHeaderParam = new JButton("Inject each Header parameter if method is Header");
        JButton labelIsCheckingAllCookieParam = new JButton("Inject each Cookie parameter");
        JButton labelIsCheckingAllJSONParam = new JButton("Inject JSON parameters");
        JButton labelIsCheckingAllSOAPParam = new JButton("Inject SOAP parameters in Request body");
        
        JLabel emptyLabelGeneralInjection = new JLabel();
        JLabel labelGeneralInjection = new JLabel("<html><b>Connection definition</b></html>");
        JLabel emptyLabelParamsInjection = new JLabel();
        JLabel labelParamsInjection = new JLabel("<html><br /><b>Parameters injection</b></html>");
        JLabel emptyLabelSessionManagement = new JLabel();
        JLabel labelSessionManagement = new JLabel("<html><br /><b>Session and Cookie management</b></html>");
        JLabel emptyLabelValueInjection = new JLabel();
        JLabel labelValueInjection = new JLabel("<html><br /><b>Special parameters</b></html>");
        
        ActionListener actionListenerCheckingAllParam = actionEvent -> {
            if (actionEvent.getSource() != this.checkboxIsCheckingAllParam) {
                this.checkboxIsCheckingAllParam.setSelected(!this.checkboxIsCheckingAllParam.isSelected());
            }
            
            this.checkboxIsCheckingAllURLParam.setSelected(this.checkboxIsCheckingAllParam.isSelected());
            this.checkboxIsCheckingAllRequestParam.setSelected(this.checkboxIsCheckingAllParam.isSelected());
            this.checkboxIsCheckingAllHeaderParam.setSelected(this.checkboxIsCheckingAllParam.isSelected());
            
            this.checkboxIsCheckingAllURLParam.setEnabled(!this.checkboxIsCheckingAllParam.isSelected());
            this.checkboxIsCheckingAllRequestParam.setEnabled(!this.checkboxIsCheckingAllParam.isSelected());
            this.checkboxIsCheckingAllHeaderParam.setEnabled(!this.checkboxIsCheckingAllParam.isSelected());
            
            labelIsCheckingAllURLParam.setEnabled(!this.checkboxIsCheckingAllParam.isSelected());
            labelIsCheckingAllRequestParam.setEnabled(!this.checkboxIsCheckingAllParam.isSelected());
            labelIsCheckingAllHeaderParam.setEnabled(!this.checkboxIsCheckingAllParam.isSelected());
            
            this.actionListenerSave.actionPerformed(null);
        };
        
        this.checkboxIsCheckingAllURLParam.setEnabled(!this.checkboxIsCheckingAllParam.isSelected());
        this.checkboxIsCheckingAllRequestParam.setEnabled(!this.checkboxIsCheckingAllParam.isSelected());
        this.checkboxIsCheckingAllHeaderParam.setEnabled(!this.checkboxIsCheckingAllParam.isSelected());
        
        labelIsCheckingAllURLParam.setEnabled(!this.checkboxIsCheckingAllParam.isSelected());
        labelIsCheckingAllRequestParam.setEnabled(!this.checkboxIsCheckingAllParam.isSelected());
        labelIsCheckingAllHeaderParam.setEnabled(!this.checkboxIsCheckingAllParam.isSelected());
        
        labelIsCheckingAllParam.addActionListener(actionListenerCheckingAllParam);
        labelIsCheckingAllURLParam.addActionListener(actionEvent -> {
            this.checkboxIsCheckingAllURLParam.setSelected(!this.checkboxIsCheckingAllURLParam.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        labelIsCheckingAllRequestParam.addActionListener(actionEvent -> {
            this.checkboxIsCheckingAllRequestParam.setSelected(!this.checkboxIsCheckingAllRequestParam.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        labelIsCheckingAllHeaderParam.addActionListener(actionEvent -> {
            this.checkboxIsCheckingAllHeaderParam.setSelected(!this.checkboxIsCheckingAllHeaderParam.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        labelIsCheckingAllJSONParam.addActionListener(actionEvent -> {
            this.checkboxIsCheckingAllJSONParam.setSelected(!this.checkboxIsCheckingAllJSONParam.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        labelIsCheckingAllCookieParam.addActionListener(actionEvent -> {
            this.checkboxIsCheckingAllCookieParam.setSelected(!this.checkboxIsCheckingAllCookieParam.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        labelIsCheckingAllSOAPParam.addActionListener(actionEvent -> {
            this.checkboxIsCheckingAllSOAPParam.setSelected(!this.checkboxIsCheckingAllSOAPParam.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        
        // Proxy label
        JLabel labelProxyAddress = new JLabel("Proxy address  ");
        JLabel labelProxyPort = new JLabel("Proxy port  ");
        JLabel labelProxyAddressHttps = new JLabel("Proxy address  ");
        JLabel labelProxyPortHttps = new JLabel("Proxy port  ");
        JButton buttonIsUsingProxy = new JButton("Use a proxy for http:// URLs");
        JButton buttonIsUsingProxyHttps = new JButton("Use a proxy for https:// URLs");
        String tooltipIsUsingProxy = "Enable proxy communication (e.g. TOR with Privoxy or Burp) for HTTP protocol.";
        buttonIsUsingProxy.setToolTipText(tooltipIsUsingProxy);
        String tooltipIsUsingProxyHttps = "Enable proxy communication (e.g. TOR with Privoxy or Burp) for HTTPS protocol.";
        buttonIsUsingProxyHttps.setToolTipText(tooltipIsUsingProxyHttps);

        // Proxy setting: IP, port, checkbox to activate proxy
        this.checkboxIsUsingProxy.setToolTipText(tooltipIsUsingProxy);
        this.checkboxIsUsingProxy.setFocusable(false);

        buttonIsUsingProxy.addActionListener(actionEvent -> {
            this.checkboxIsUsingProxy.setSelected(!this.checkboxIsUsingProxy.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        
        this.checkboxIsUsingProxyHttps.setToolTipText(tooltipIsUsingProxyHttps);
        this.checkboxIsUsingProxyHttps.setFocusable(false);
        
        buttonIsUsingProxyHttps.addActionListener(actionEvent -> {
            this.checkboxIsUsingProxyHttps.setSelected(!this.checkboxIsUsingProxyHttps.isSelected());
            this.actionListenerSave.actionPerformed(null);
        });
        
        // Digest label
        JLabel labelDigestAuthenticationUsername = new JLabel("Username  ");
        JLabel labelDigestAuthenticationPassword = new JLabel("Password  ");
        final JButton labelUseDigestAuthentication = new JButton("Enable Basic, Digest, NTLM");
        String tooltipUseDigestAuthentication =
            "<html>"
            + "Enable <b>Basic</b>, <b>Digest</b>, <b>NTLM</b> authentication (e.g. WWW-Authenticate).<br>"
            + "Then define username and password for the host.<br>"
            + "<i><b>Negotiate</b> authentication is defined in URL.</i>"
            + "</html>";
        labelUseDigestAuthentication.setToolTipText(tooltipUseDigestAuthentication);
        
        // Proxy setting: IP, port, checkbox to activate proxy
        this.checkboxUseDigestAuthentication.setToolTipText(tooltipUseDigestAuthentication);
        this.checkboxUseDigestAuthentication.setFocusable(false);
        
        // Digest label
        JLabel labelKerberosLoginConf = new JLabel("login.conf  ");
        JLabel labelKerberosKrb5Conf = new JLabel("krb5.conf  ");
        final JButton labelUseKerberos = new JButton("Enable Kerberos");
        String tooltipUseKerberos =
            "<html>"
            + "Activate Kerberos authentication, then define path to <b>login.conf</b> and <b>krb5.conf</b>.<br>"
            + "Path to <b>.keytab</b> file is defined in login.conf ; name of <b>principal</b> must be correct.<br>"
            + "<b>Realm</b> and <b>kdc</b> are defined in krb5.conf.<br>"
            + "Finally use the <b>correct hostname</b> in URL, e.g. http://servicename.corp.test/[..]"
            + "</html>";
        labelUseKerberos.setToolTipText(tooltipUseKerberos);
        
        // Proxy setting: IP, port, checkbox to activate proxy
        this.textKerberosLoginConf.setToolTipText(
            "<html>"
            + "Define the path to <b>login.conf</b>. Sample :<br>"
            + "&emsp;<b>entry-name</b> {<br>"
            + "&emsp;&emsp;com.sun.security.auth.module.Krb5LoginModule<br>"
            + "&emsp;&emsp;required<br>"
            + "&emsp;&emsp;useKeyTab=true<br>"
            + "&emsp;&emsp;keyTab=\"<b>/path/to/my.keytab</b>\"<br>"
            + "&emsp;&emsp;principal=\"<b>HTTP/SERVICENAME.CORP.TEST@CORP.TEST</b>\"<br>"
            + "&emsp;&emsp;debug=false;<br>"
            + "&emsp;}<br>"
            + "<i>Principal name is case sensitive ; entry-name is read automatically.</i>"
            + "</html>");
        this.textKerberosKrb5Conf.setToolTipText(
            "<html>"
            + "Define the path to <b>krb5.conf</b>. Sample :<br>"
            + "&emsp;[libdefaults]<br>"
            + "&emsp;&emsp;default_realm = <b>CORP.TEST</b><br>"
            + "&emsp;&emsp;udp_preference_limit = 1<br>"
            + "&emsp;[realms]<br>"
            + "&emsp;&emsp;<b>CORP.TEST</b> = {<br>"
            + "&emsp;&emsp;&emsp;kdc = <b>127.0.0.1:88</b><br>"
            + "&emsp;&emsp;}<br>"
            + "<i>Realm and kdc are case sensitives.</i>"
            + "</html>");
        this.checkboxUseKerberos.setToolTipText(tooltipUseKerberos);
        this.checkboxUseKerberos.setFocusable(false);
        
        labelUseKerberos.addActionListener(actionEvent -> {
            this.checkboxUseKerberos.setSelected(!this.checkboxUseKerberos.isSelected());
            if (this.checkboxUseKerberos.isSelected()) {
                this.checkboxUseDigestAuthentication.setSelected(false);
            }
            this.actionListenerSave.actionPerformed(null);
        });
        
        labelUseDigestAuthentication.addActionListener(actionEvent -> {
            this.checkboxUseDigestAuthentication.setSelected(!this.checkboxUseDigestAuthentication.isSelected());
            if (this.checkboxUseDigestAuthentication.isSelected()) {
                this.checkboxUseKerberos.setSelected(false);
            }
            this.actionListenerSave.actionPerformed(null);
        });
        
        this.textKerberosKrb5Conf.setMaximumSize(new Dimension(400, 0));
        this.textKerberosLoginConf.setMaximumSize(new Dimension(400, 0));
        this.textDigestAuthenticationUsername.setMaximumSize(new Dimension(200, 0));
        this.textDigestAuthenticationPassword.setMaximumSize(new Dimension(200, 0));
        this.textProxyAddress.setMaximumSize(new Dimension(200, 0));
        this.textProxyPort.setMaximumSize(new Dimension(200, 0));
        this.textProxyAddressHttps.setMaximumSize(new Dimension(200, 0));
        this.textProxyPortHttps.setMaximumSize(new Dimension(200, 0));
        
        this.textProxyAddress.setFont(HelperUi.FONT_SEGOE_BIG);
        this.textProxyPort.setFont(HelperUi.FONT_SEGOE_BIG);
        this.textProxyAddressHttps.setFont(HelperUi.FONT_SEGOE_BIG);
        this.textProxyPortHttps.setFont(HelperUi.FONT_SEGOE_BIG);
        this.textKerberosLoginConf.setFont(HelperUi.FONT_SEGOE_BIG);
        this.textKerberosKrb5Conf.setFont(HelperUi.FONT_SEGOE_BIG);
        
        this.textDigestAuthenticationUsername.setFont(HelperUi.FONT_SEGOE_BIG);
        this.textDigestAuthenticationPassword.setFont(HelperUi.FONT_SEGOE_BIG);
        
        this.checkboxIsFollowingRedirection.addActionListener(this.actionListenerSave);
        this.checkboxIsNotInjectingMetadata.addActionListener(this.actionListenerSave);
        this.checkboxIsUsingProxy.addActionListener(this.actionListenerSave);
        this.checkboxIsParsingForm.addActionListener(this.actionListenerSave);
        this.checkboxIsNotTestingConnection.addActionListener(this.actionListenerSave);
        this.checkboxUseDigestAuthentication.addActionListener(this.actionListenerSave);
        this.checkboxUseKerberos.addActionListener(this.actionListenerSave);
        this.checkboxProcessCookies.addActionListener(this.actionListenerSave);
        this.checkboxProcessCsrf.addActionListener(this.actionListenerSave);
        
        this.checkboxIsCheckingAllParam.addActionListener(actionListenerCheckingAllParam);
        this.checkboxIsCheckingAllURLParam.addActionListener(this.actionListenerSave);
        this.checkboxIsCheckingAllRequestParam.addActionListener(this.actionListenerSave);
        this.checkboxIsCheckingAllHeaderParam.addActionListener(this.actionListenerSave);
        this.checkboxIsCheckingAllJSONParam.addActionListener(this.actionListenerSave);
        this.checkboxIsCheckingAllCookieParam.addActionListener(this.actionListenerSave);
        this.checkboxIsCheckingAllSOAPParam.addActionListener(this.actionListenerSave);
        
        this.checkboxIsTamperingEval.addActionListener(this.actionListenerSave);
        this.checkboxIsTamperingBase64.addActionListener(this.actionListenerSave);
        this.checkboxIsTamperingFunctionComment.addActionListener(this.actionListenerSave);
        this.checkboxIsTamperingVersionComment.addActionListener(this.actionListenerSave);
        this.checkboxIsTamperingEqualToLike.addActionListener(this.actionListenerSave);
        this.checkboxIsTamperingRandomCase.addActionListener(this.actionListenerSave);
        this.checkboxIsTamperingHexToChar.addActionListener(this.actionListenerSave);
        this.checkboxIsTamperingQuoteToUtf8.addActionListener(this.actionListenerSave);
        this.radioIsTamperingSpaceToMultilineComment.addActionListener(this.actionListenerSave);
        this.radioIsTamperingSpaceToDashComment.addActionListener(this.actionListenerSave);
        this.radioIsTamperingSpaceToSharpComment.addActionListener(this.actionListenerSave);
        
        class DocumentListenerSave extends DocumentListenerTyping {
            
            @Override
            public void warn() {
                PanelPreferences.this.actionListenerSave.actionPerformed(null);
            }
            
        }
        
        DocumentListener documentListenerSave = new DocumentListenerSave();

        this.textDigestAuthenticationPassword.getDocument().addDocumentListener(documentListenerSave);
        this.textDigestAuthenticationUsername.getDocument().addDocumentListener(documentListenerSave);
        this.textKerberosKrb5Conf.getDocument().addDocumentListener(documentListenerSave);
        this.textKerberosLoginConf.getDocument().addDocumentListener(documentListenerSave);
        this.textProxyAddress.getDocument().addDocumentListener(documentListenerSave);
        this.textProxyPort.getDocument().addDocumentListener(documentListenerSave);
        this.textProxyAddressHttps.getDocument().addDocumentListener(documentListenerSave);
        this.textProxyPortHttps.getDocument().addDocumentListener(documentListenerSave);

        labelIsCheckingUpdate.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsCheckingUpdate.setBorderPainted(false);
        labelIsCheckingUpdate.setContentAreaFilled(false);
        
        labelIsReportingBugs.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsReportingBugs.setBorderPainted(false);
        labelIsReportingBugs.setContentAreaFilled(false);
        
        labelIsFollowingRedirection.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsFollowingRedirection.setBorderPainted(false);
        labelIsFollowingRedirection.setContentAreaFilled(false);
        
        labelTestConnection.setHorizontalAlignment(SwingConstants.LEFT);
        labelTestConnection.setBorderPainted(false);
        labelTestConnection.setContentAreaFilled(false);
        
        labelParseForm.setHorizontalAlignment(SwingConstants.LEFT);
        labelParseForm.setBorderPainted(false);
        labelParseForm.setContentAreaFilled(false);
        
        buttonIsUsingProxy.setHorizontalAlignment(SwingConstants.LEFT);
        buttonIsUsingProxy.setBorderPainted(false);
        buttonIsUsingProxy.setContentAreaFilled(false);
        
        buttonIsUsingProxyHttps.setHorizontalAlignment(SwingConstants.LEFT);
        buttonIsUsingProxyHttps.setBorderPainted(false);
        buttonIsUsingProxyHttps.setContentAreaFilled(false);
        
        labelUseDigestAuthentication.setHorizontalAlignment(SwingConstants.LEFT);
        labelUseDigestAuthentication.setBorderPainted(false);
        labelUseDigestAuthentication.setContentAreaFilled(false);
        
        labelUseKerberos.setHorizontalAlignment(SwingConstants.LEFT);
        labelUseKerberos.setBorderPainted(false);
        labelUseKerberos.setContentAreaFilled(false);
        
        labelIsNotInjectingMetadata.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsNotInjectingMetadata.setBorderPainted(false);
        labelIsNotInjectingMetadata.setContentAreaFilled(false);
        
        labelIsCheckingAllParam.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsCheckingAllParam.setBorderPainted(false);
        labelIsCheckingAllParam.setContentAreaFilled(false);
        
        labelIsCheckingAllURLParam.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsCheckingAllURLParam.setBorderPainted(false);
        labelIsCheckingAllURLParam.setContentAreaFilled(false);
        
        labelIsCheckingAllRequestParam.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsCheckingAllRequestParam.setBorderPainted(false);
        labelIsCheckingAllRequestParam.setContentAreaFilled(false);
        
        labelIsCheckingAllHeaderParam.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsCheckingAllHeaderParam.setBorderPainted(false);
        labelIsCheckingAllHeaderParam.setContentAreaFilled(false);
        
        labelIsCheckingAllJSONParam.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsCheckingAllJSONParam.setBorderPainted(false);
        labelIsCheckingAllJSONParam.setContentAreaFilled(false);

        labelIsCheckingAllCookieParam.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsCheckingAllCookieParam.setBorderPainted(false);
        labelIsCheckingAllCookieParam.setContentAreaFilled(false);
        
        labelIsCheckingAllSOAPParam.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsCheckingAllSOAPParam.setBorderPainted(false);
        labelIsCheckingAllSOAPParam.setContentAreaFilled(false);
        
        labelProcessCookies.setHorizontalAlignment(SwingConstants.LEFT);
        labelProcessCookies.setBorderPainted(false);
        labelProcessCookies.setContentAreaFilled(false);
        
        labelProcessCsrf.setHorizontalAlignment(SwingConstants.LEFT);
        labelProcessCsrf.setBorderPainted(false);
        labelProcessCsrf.setContentAreaFilled(false);
        
        labelIsTamperingBase64.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsTamperingBase64.setBorderPainted(false);
        labelIsTamperingBase64.setContentAreaFilled(false);
        
        labelIsTamperingFunctionComment.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsTamperingFunctionComment.setBorderPainted(false);
        labelIsTamperingFunctionComment.setContentAreaFilled(false);
        
        labelIsTamperingVersionComment.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsTamperingVersionComment.setBorderPainted(false);
        labelIsTamperingVersionComment.setContentAreaFilled(false);
        
        labelIsTamperingEqualToLike.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsTamperingEqualToLike.setBorderPainted(false);
        labelIsTamperingEqualToLike.setContentAreaFilled(false);
        
        labelIsTamperingRandomCase.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsTamperingRandomCase.setBorderPainted(false);
        labelIsTamperingRandomCase.setContentAreaFilled(false);
        
        labelIsTamperingHexToChar.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsTamperingHexToChar.setBorderPainted(false);
        labelIsTamperingHexToChar.setContentAreaFilled(false);
        
        labelIsTamperingQuoteToUtf8.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsTamperingQuoteToUtf8.setBorderPainted(false);
        labelIsTamperingQuoteToUtf8.setContentAreaFilled(false);
        
        labelIsTamperingSpaceToMultilineComment.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsTamperingSpaceToMultilineComment.setBorderPainted(false);
        labelIsTamperingSpaceToMultilineComment.setContentAreaFilled(false);
        
        labelIsTamperingSpaceToDashComment.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsTamperingSpaceToDashComment.setBorderPainted(false);
        labelIsTamperingSpaceToDashComment.setContentAreaFilled(false);
        
        labelIsTamperingSpaceToSharpComment.setHorizontalAlignment(SwingConstants.LEFT);
        labelIsTamperingSpaceToSharpComment.setBorderPainted(false);
        labelIsTamperingSpaceToSharpComment.setContentAreaFilled(false);
        
        labelIs4K.setHorizontalAlignment(SwingConstants.LEFT);
        labelIs4K.setBorderPainted(false);
        labelIs4K.setContentAreaFilled(false);
        
        // Proxy settings, Horizontal column rules

        groupLayoutTampering.setHorizontalGroup(
            groupLayoutTampering.createSequentialGroup()
            .addGroup(
                groupLayoutTampering
                    .createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                    .addComponent(this.checkboxIsTamperingBase64)
                    .addComponent(this.checkboxIsTamperingFunctionComment)
                    .addComponent(this.checkboxIsTamperingVersionComment)
                    .addComponent(this.checkboxIsTamperingEqualToLike)
                    .addComponent(this.checkboxIsTamperingRandomCase)
                    .addComponent(this.checkboxIsTamperingHexToChar)
                    .addComponent(this.checkboxIsTamperingQuoteToUtf8)
                    .addComponent(this.radioIsTamperingSpaceToMultilineComment)
                    .addComponent(this.radioIsTamperingSpaceToDashComment)
                    .addComponent(this.radioIsTamperingSpaceToSharpComment)
                    .addComponent(this.checkboxIsTamperingEval)
            ).addGroup(
                groupLayoutTampering
                    .createParallelGroup()
                    .addComponent(labelIsTamperingBase64)
                    .addComponent(labelIsTamperingFunctionComment)
                    .addComponent(labelIsTamperingVersionComment)
                    .addComponent(labelIsTamperingEqualToLike)
                    .addComponent(labelIsTamperingRandomCase)
                    .addComponent(labelIsTamperingHexToChar)
                    .addComponent(labelIsTamperingQuoteToUtf8)
                    .addComponent(labelIsTamperingSpaceToMultilineComment)
                    .addComponent(labelIsTamperingSpaceToDashComment)
                    .addComponent(labelIsTamperingSpaceToSharpComment)
                    .addComponent(textAreaIsTamperingEval)
        ));
        
        groupLayoutGeneral.setHorizontalGroup(
            groupLayoutGeneral.createSequentialGroup()
                .addGroup(
                    groupLayoutGeneral
                        .createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                ).addGroup(
                    groupLayoutGeneral
                        .createParallelGroup()
                        .addComponent(labelIsCheckingUpdate)
                        .addComponent(labelIsReportingBugs)
                        .addComponent(labelIs4K)
                ));
        
        groupLayoutInjection.setHorizontalGroup(
            groupLayoutInjection.createSequentialGroup()
            .addGroup(
                groupLayoutInjection
                    .createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                    .addComponent(emptyLabelGeneralInjection)
                    .addComponent(this.checkboxIsFollowingRedirection)
                    .addComponent(this.checkboxIsNotTestingConnection)
                    .addComponent(this.checkboxIsParsingForm)
                    .addComponent(this.checkboxIsNotInjectingMetadata)
                    
                    .addComponent(emptyLabelParamsInjection)
                    .addComponent(this.checkboxIsCheckingAllParam)
                    .addComponent(this.checkboxIsCheckingAllURLParam)
                    .addComponent(this.checkboxIsCheckingAllRequestParam)
                    .addComponent(this.checkboxIsCheckingAllHeaderParam)
                    
                    .addComponent(emptyLabelValueInjection)
                    .addComponent(this.checkboxIsCheckingAllCookieParam)
                    .addComponent(this.checkboxIsCheckingAllJSONParam)
                    .addComponent(this.checkboxIsCheckingAllSOAPParam)
                    
                    .addComponent(emptyLabelSessionManagement)
                    .addComponent(this.checkboxProcessCsrf)
                    .addComponent(this.checkboxProcessCookies)
            ).addGroup(
                groupLayoutInjection
                    .createParallelGroup()
                    .addComponent(labelGeneralInjection)
                    .addComponent(labelIsFollowingRedirection)
                    .addComponent(labelTestConnection)
                    .addComponent(labelParseForm)
                    .addComponent(labelIsNotInjectingMetadata)
                    
                    .addComponent(labelParamsInjection)
                    .addComponent(labelIsCheckingAllParam)
                    .addComponent(labelIsCheckingAllURLParam)
                    .addComponent(labelIsCheckingAllRequestParam)
                    .addComponent(labelIsCheckingAllHeaderParam)
                    
                    .addComponent(labelValueInjection)
                    .addComponent(labelIsCheckingAllJSONParam)
                    .addComponent(labelIsCheckingAllSOAPParam)
                    .addComponent(labelIsCheckingAllCookieParam)
                    
                    .addComponent(labelSessionManagement)
                    .addComponent(labelProcessCsrf)
                    .addComponent(labelProcessCookies)
        ));

        groupLayoutAuthentication.setHorizontalGroup(
            groupLayoutAuthentication.createSequentialGroup()
            .addGroup(
                groupLayoutAuthentication
                    .createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                    .addComponent(this.checkboxUseDigestAuthentication)
                    .addComponent(labelDigestAuthenticationUsername)
                    .addComponent(labelDigestAuthenticationPassword)
                    .addComponent(this.checkboxUseKerberos)
                    .addComponent(labelKerberosLoginConf)
                    .addComponent(labelKerberosKrb5Conf)
            ).addGroup(
                groupLayoutAuthentication
                    .createParallelGroup()
                    .addComponent(labelUseDigestAuthentication)
                    .addComponent(this.textDigestAuthenticationUsername)
                    .addComponent(this.textDigestAuthenticationPassword)
                    .addComponent(labelUseKerberos)
                    .addComponent(this.textKerberosLoginConf)
                    .addComponent(this.textKerberosKrb5Conf)
        ));

        groupLayoutProxy.setHorizontalGroup(
            groupLayoutProxy.createSequentialGroup()
            .addGroup(
                groupLayoutProxy
                    .createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                    .addComponent(labelProxyHttpHidden)
                    .addComponent(this.checkboxIsUsingProxy)
                    .addComponent(labelProxyAddress)
                    .addComponent(labelProxyPort)
                    .addComponent(labelProxyHttpsHidden)
                    .addComponent(this.checkboxIsUsingProxyHttps)
                    .addComponent(labelProxyAddressHttps)
                    .addComponent(labelProxyPortHttps)
            ).addGroup(
                groupLayoutProxy
                    .createParallelGroup()
                    .addComponent(labelProxyHttp)
                    .addComponent(buttonIsUsingProxy)
                    .addComponent(this.textProxyAddress)
                    .addComponent(this.textProxyPort)
                    .addComponent(labelProxyHttps)
                    .addComponent(buttonIsUsingProxyHttps)
                    .addComponent(this.textProxyAddressHttps)
                    .addComponent(this.textProxyPortHttps)
        ));

        // Proxy settings, Vertical line rules

        groupLayoutTampering.setVerticalGroup(
            groupLayoutTampering
                .createSequentialGroup()
                .addGroup(
                    groupLayoutTampering
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsTamperingBase64)
                        .addComponent(labelIsTamperingBase64)
                ).addGroup(
                    groupLayoutTampering
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsTamperingFunctionComment)
                        .addComponent(labelIsTamperingFunctionComment)
                ).addGroup(
                    groupLayoutTampering
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsTamperingVersionComment)
                        .addComponent(labelIsTamperingVersionComment)
                ).addGroup(
                    groupLayoutTampering
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsTamperingEqualToLike)
                        .addComponent(labelIsTamperingEqualToLike)
                ).addGroup(
                    groupLayoutTampering
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsTamperingRandomCase)
                        .addComponent(labelIsTamperingRandomCase)
                ).addGroup(
                    groupLayoutTampering
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsTamperingHexToChar)
                        .addComponent(labelIsTamperingHexToChar)
                ).addGroup(
                        groupLayoutTampering
                            .createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(this.checkboxIsTamperingQuoteToUtf8)
                            .addComponent(labelIsTamperingQuoteToUtf8)
                ).addGroup(
                    groupLayoutTampering
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.radioIsTamperingSpaceToMultilineComment)
                        .addComponent(labelIsTamperingSpaceToMultilineComment)
                ).addGroup(
                    groupLayoutTampering
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.radioIsTamperingSpaceToDashComment)
                        .addComponent(labelIsTamperingSpaceToDashComment)
                ).addGroup(
                    groupLayoutTampering
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.radioIsTamperingSpaceToSharpComment)
                        .addComponent(labelIsTamperingSpaceToSharpComment)
                ).addGroup(
                    groupLayoutTampering
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsTamperingEval)
                        .addComponent(textAreaIsTamperingEval)
                )
        );
    
        groupLayoutGeneral.setVerticalGroup(
            groupLayoutGeneral
                .createSequentialGroup()
                .addGroup(
                    groupLayoutGeneral
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelIsCheckingUpdate)
                ).addGroup(
                    groupLayoutGeneral
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        //.addComponent(this.checkboxIsReportingBugs)
                        .addComponent(labelIsReportingBugs)
                ).addGroup(
                    groupLayoutGeneral
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                       // .addComponent(this.checkboxIs4K)
                        .addComponent(labelIs4K)
                )
        );
        
        groupLayoutInjection.setVerticalGroup(
            groupLayoutInjection
                .createSequentialGroup()
                .addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(emptyLabelGeneralInjection)
                        .addComponent(labelGeneralInjection)
                ).addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsFollowingRedirection)
                        .addComponent(labelIsFollowingRedirection)
                ).addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsNotTestingConnection)
                        .addComponent(labelTestConnection)
                ).addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsParsingForm)
                        .addComponent(labelParseForm)
                ).addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsNotInjectingMetadata)
                        .addComponent(labelIsNotInjectingMetadata)
                        
                ).addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(emptyLabelParamsInjection)
                        .addComponent(labelParamsInjection)
                ).addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsCheckingAllParam)
                        .addComponent(labelIsCheckingAllParam)
                ).addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsCheckingAllURLParam)
                        .addComponent(labelIsCheckingAllURLParam)
                ).addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsCheckingAllRequestParam)
                        .addComponent(labelIsCheckingAllRequestParam)
                ).addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsCheckingAllHeaderParam)
                        .addComponent(labelIsCheckingAllHeaderParam)
                        
                ).addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(emptyLabelValueInjection)
                        .addComponent(labelValueInjection)
                ).addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsCheckingAllJSONParam)
                        .addComponent(labelIsCheckingAllJSONParam)
                ).addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsCheckingAllSOAPParam)
                        .addComponent(labelIsCheckingAllSOAPParam)
                ).addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsCheckingAllCookieParam)
                        .addComponent(labelIsCheckingAllCookieParam)
                        
                ).addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(emptyLabelSessionManagement)
                        .addComponent(labelSessionManagement)
                ).addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxProcessCsrf)
                        .addComponent(labelProcessCsrf)
                ).addGroup(
                    groupLayoutInjection
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxProcessCookies)
                        .addComponent(labelProcessCookies)
                )
        );

        groupLayoutAuthentication.setVerticalGroup(
            groupLayoutAuthentication
                .createSequentialGroup()
                .addGroup(
                    groupLayoutAuthentication
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxUseDigestAuthentication)
                        .addComponent(labelUseDigestAuthentication)
                ).addGroup(
                    groupLayoutAuthentication
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelDigestAuthenticationUsername)
                        .addComponent(this.textDigestAuthenticationUsername)
                ).addGroup(
                    groupLayoutAuthentication
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelDigestAuthenticationPassword)
                        .addComponent(this.textDigestAuthenticationPassword)
                ).addGroup(
                    groupLayoutAuthentication
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxUseKerberos)
                        .addComponent(labelUseKerberos)
                ).addGroup(
                    groupLayoutAuthentication
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelKerberosLoginConf)
                        .addComponent(this.textKerberosLoginConf)
                ).addGroup(
                    groupLayoutAuthentication
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelKerberosKrb5Conf)
                        .addComponent(this.textKerberosKrb5Conf)
                )
        );

        groupLayoutProxy.setVerticalGroup(
            groupLayoutProxy
                .createSequentialGroup()
                .addGroup(
                    groupLayoutProxy
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelProxyHttpHidden)
                        .addComponent(labelProxyHttp)
                )
                .addGroup(
                    groupLayoutProxy
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsUsingProxy)
                        .addComponent(buttonIsUsingProxy)
                ).addGroup(
                    groupLayoutProxy
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelProxyAddress)
                        .addComponent(this.textProxyAddress)
                ).addGroup(
                    groupLayoutProxy
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelProxyPort)
                        .addComponent(this.textProxyPort)
                )
                .addGroup(
                    groupLayoutProxy
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelProxyHttpsHidden)
                        .addComponent(labelProxyHttps)
                )
                .addGroup(
                    groupLayoutProxy
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.checkboxIsUsingProxyHttps)
                        .addComponent(buttonIsUsingProxyHttps)
                ).addGroup(
                    groupLayoutProxy
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelProxyAddressHttps)
                        .addComponent(this.textProxyAddressHttps)
                ).addGroup(
                    groupLayoutProxy
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelProxyPortHttps)
                        .addComponent(this.textProxyPortHttps)
                )
        );
        
        this.add(panelInjection, BorderLayout.CENTER);
        
        JList<CategoryPreference> categories = new JList<>(CategoryPreference.values());
        categories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categories.setSelectedIndex(0);
        
        categories.setBorder(BorderFactory.createLineBorder(HelperUi.COLOR_COMPONENT_BORDER));
        categories.addListSelectionListener(e -> {
            PanelPreferences.this.remove(borderLayoutPreferences.getLayoutComponent(BorderLayout.CENTER));
            PanelPreferences.this.add(categories.getSelectedValue().getPanel(), BorderLayout.CENTER);
            // Both required
            PanelPreferences.this.revalidate();
            PanelPreferences.this.repaint();
        });
        
        categories.setCellRenderer(new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel labelItemList = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                labelItemList.setBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(3, 3, 0, 3, Color.WHITE),
                        labelItemList.getBorder()
                    )
                );
                return labelItemList;
            }
            
        });
        
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(categories, BorderLayout.LINE_START);
    }
    
}
