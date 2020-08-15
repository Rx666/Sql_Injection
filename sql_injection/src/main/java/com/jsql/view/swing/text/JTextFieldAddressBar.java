package com.jsql.view.swing.text;

import javax.swing.JTextField;

import com.jsql.view.swing.HelperUi;

/**
 * 定义包含图标和按钮的 JTextField 
 */
@SuppressWarnings("serial")
public class JTextFieldAddressBar extends JPopupTextField implements DecoratorJComponent<JTextField> {
    
    /**
     * Constructor with default text.
     * @param string The text to display
     */
    public JTextFieldAddressBar(JTextField c) {
        super(c);
        this.getProxy().setFont(HelperUi.FONT_SEGOE_BIG);
    }
    
}
