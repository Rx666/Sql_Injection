package com.jsql.view.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;

import org.apache.log4j.Logger;

import com.jsql.view.swing.console.SwingAppender;
import com.jsql.view.swing.ui.CheckBoxIcon;
import com.jsql.view.swing.ui.CustomBasicComboBoxUI;

/**
 * 	构建默认的组件外观，键盘快捷键和图标。
 */
@SuppressWarnings("serial")
public final class HelperUi {
    
    private static final Logger LOGGER = Logger.getRootLogger();

    public static final Color COLOR_BLU = new Color(132, 172, 221);
    public static final Color COLOR_GREEN = new Color(0, 128, 0);

    public static final Color COLOR_FOCUS_GAINED = (Color) UIManager.get("TabbedPane.selected");
    
    public static final Color COLOR_DEFAULT_BACKGROUND = UIManager.getColor("Panel.background");
    public static final Color COLOR_COMPONENT_BORDER = UIManager.getColor("controlShadow");
    public static final Color COLOR_FOCUS_LOST = new Color(248, 249, 249);
    public static final Border BORDER_FOCUS_LOST = new LineBorder(new Color(218, 218, 218), 1, false);
    public static final Border BORDER_FOCUS_GAINED = new LineBorder(HelperUi.COLOR_BLU, 1, false);
    
    public static final URL URL_GLOBE = HelperUi.class.getClassLoader().getResource("swing/images/icons/globe.png");
    
    public static final Icon ICON_TICK = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/check.png"));
    public static final Icon ICON_SQUARE_RED = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/squareRed.png"));
    public static final Icon ICON_SQUARE_GREY = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/squareGrey.png"));
    public static final Icon ICON_LOADER_GIF = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/spinner.gif"));

    public static final Icon ICON_ADMIN_SERVER = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/admin.png"));
    public static final Icon ICON_SHELL_SERVER = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/application_xp_terminal.png"));
    public static final Icon ICON_DATABASE_SERVER = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/database.png"));
    public static final Icon ICON_FILE_SERVER = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/file.png"));
    public static final Icon ICON_BRUTER = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/lock_open.png"));
    public static final Icon ICON_CODER = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/textfield.png"));
    public static final Icon ICON_UPLOAD = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/disk.png"));
    public static final Icon ICON_SCANLIST = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/table_multiple.png"));

    public static final Icon ICON_TABLE = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/table.png"));
    public static final Icon ICON_TABLE_GO = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/tableGo.png"));
    public static final Icon ICON_DATABASE = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/database.png"));
    public static final Icon ICON_DATABASE_GO = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/databaseGo.png"));
    
    public static final Icon ICON_CONSOLE = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/console.gif"));
    public static final Icon ICON_HEADER = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/header.gif"));
    public static final Icon ICON_CHUNK = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/chunk.gif"));
    public static final Icon ICON_BINARY = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/binary.gif"));
    public static final Icon ICON_CUP = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/cup.png"));
    
    public static final Icon ICON_CLOSE = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/close.png"));
    public static final Icon ICON_CLOSE_ROLLOVER = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/closeRollover.png"));
    public static final Icon ICON_CLOSE_PRESSED = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/closePressed.png"));

    public static final Icon ICON_CRAWLER_DEFAULT = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/crawlerDefault.png"));
    public static final Icon ICON_CRAWLER_ROLLOVER = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/crawlerRollover.png"));
    public static final Icon ICON_CRAWLER_PRESSED = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/crawlerPressed.png"));
    
    public static final Icon ICON_CRAWLER_RESET_DEFAULT = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/resetCrawlDefault.png"));
    public static final Icon ICON_CRAWLER_RESET_PRESSED = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/resetCrawlPressed.png"));
    
    public static final Icon ICON_ARROW_DEFAULT = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/arrowDefault.png"));
    public static final Icon ICON_ARROW_ROLLOVER = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/arrowRollover.png"));
    public static final Icon ICON_ARROW_PRESSED = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/arrowPressed.png"));

    
    public static final URL URL_ICON_16 = HelperUi.class.getClassLoader().getResource("swing/images/software/bug16.png");
    public static final URL URL_ICON_32 = HelperUi.class.getClassLoader().getResource("swing/images/software/bug32.png");
    public static final URL URL_ICON_96 = HelperUi.class.getClassLoader().getResource("swing/images/software/bug96.png");
    public static final URL URL_ICON_128 = HelperUi.class.getClassLoader().getResource("swing/images/software/bug128.png");

    /**
     * 	从具有8位alpha通道的PNG图像创建投影。
     */
    public static final Image IMG_SHADOW = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/shadow.png")).getImage();
    public static final ImageIcon IMG_BUG = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/bug.png"));
    public static final ImageIcon IMG_STOP_DEFAULT = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/stopDefault.png"));
    public static final ImageIcon IMG_STOP_ROLLOVER = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/stopRollover.png"));
    public static final ImageIcon IMG_STOP_PPRESSED = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/stopPressed.png"));
    public static final String PATH_WEB_FOLDERS = "swing/list/payload.txt";
    public static final InputStream INPUT_STREAM_PAGES_SCAN = HelperUi.class.getClassLoader().getResourceAsStream("swing/list/scan-page.json");

    public static final Icon ICON_EMPTY = new ImageIcon(new BufferedImage(16, 16, Transparency.TRANSLUCENT));

    public static final String PATH_PAUSE = "swing/images/icons/pause.png";
    public static final String PATH_PROGRESSBAR = "swing/images/icons/progressBar.gif";
    
    public static final Icon ICON_ERROR = new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/error.png"));
    
    public static final Border BORDER_BLU = BorderFactory.createCompoundBorder(
        BORDER_FOCUS_GAINED,
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    );
    
    public static final Border BORDER_ROUND_BLU = new AbstractBorder() {
        
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int r = 5;
            RoundRectangle2D round = new RoundRectangle2D.Float(x, y, width-1f, height-1f, r, r);
            Container parent = c.getParent();
            if (parent!=null) {
                g2.setColor(parent.getBackground());
                Area corner = new Area(new Rectangle2D.Float(x, y, width, height));
                corner.subtract(new Area(round));
                
                try {
                    g2.fill(corner);
                } catch (NoClassDefFoundError | UnsatisfiedLinkError e) {
                    LOGGER.error(e, e);
                }
            }
            g2.setColor(Color.GRAY);
            
            try {
                g2.draw(round);
            } catch (NoClassDefFoundError e) {
                LOGGER.error(e, e);
            }
                
            g2.dispose();
        }
        
        @Override 
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 8, 4, 8);
        }
        
        @Override 
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = 8;
            insets.top = insets.bottom = 4;
            return insets;
        }
        
    };

    public static final String FONT_NAME_UBUNTU_MONO = "Ubuntu Mono";
    public static final String FONT_NAME_UBUNTU_REGULAR = "Ubuntu";
    
    public static final String FONT_NAME_MONOSPACED = "Monospaced";

    public static final Font FONT_UBUNTU_MONO = new Font(
        HelperUi.FONT_NAME_UBUNTU_MONO,
        Font.PLAIN,
        UIManager.getDefaults().getFont("TextArea.font").getSize() + 2
    );
    
    public static final Font FONT_UBUNTU_REGULAR = new Font(
        HelperUi.FONT_NAME_UBUNTU_REGULAR,
        Font.PLAIN,
        UIManager.getDefaults().getFont("TextPane.font").getSize()
    );
    
    public static final Font FONT_SEGOE = new Font(
        "Segoe UI",
        Font.PLAIN,
        UIManager.getDefaults().getFont("TextPane.font").getSize()
    );
    
    public static final Font FONT_SEGOE_BIG = new Font(
        UIManager.getDefaults().getFont("TextField.font").getName(),
        Font.PLAIN,
        UIManager.getDefaults().getFont("TextField.font").getSize() + 2
    );

    public static final String CHUNK_VISIBLE = "chunk_visible";
    public static final String BINARY_VISIBLE = "binary_visible";
    public static final String NETWORK_VISIBLE = "header_visible";
    public static final String JAVA_VISIBLE = "java_visible";
    
    
    /**
     *	更改各种组件的默认样式
     */
    public static void prepareGUI() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try (InputStream fontStream = new BufferedInputStream(SwingAppender.class.getClassLoader().getResourceAsStream("swing/font/UbuntuMono-R-ctrlchar.ttf"))) {
            Font ubuntuFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            ge.registerFont(ubuntuFont);
        } catch (FontFormatException | IOException e) {
            LOGGER.warn("Loading Font Ubuntu Mono with control characters failed", e);
        }
        try (InputStream fontStream = new BufferedInputStream(SwingAppender.class.getClassLoader().getResourceAsStream("swing/font/Ubuntu-R.ttf"))) {
            Font ubuntuFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            ge.registerFont(ubuntuFont);
        } catch (FontFormatException | IOException e) {
            LOGGER.warn("Loading Font Ubuntu failed", e);
        }
        
        // 自定义 tooltip
        // 显示工具提示之前的计时器
        ToolTipManager.sharedInstance().setInitialDelay(750);
        // 自动关闭之前的计时器工具提示
        ToolTipManager.sharedInstance().setDismissDelay(30000);
        // 鼠标移至其他组件时使用的计时器，如果计时器未过期，则立即显示工具提示
        ToolTipManager.sharedInstance().setReshowDelay(1);

        UIManager.put("ToolTip.background", new Color(255, 255, 225));
        UIManager.put("ToolTip.backgroundInactive", new Color(255, 255, 225));
        UIManager.put("ToolTip.foreground", Color.BLACK);
        UIManager.put("ToolTip.foregroundInactive", Color.BLACK);
        UIManager.put("ToolTip.font", HelperUi.FONT_SEGOE);
        
        // 自定义 button
        // 在默认的"另存为"，确认对话框中更改按钮的边框
        UIManager.put("Button.border", HelperUi.BORDER_BLU);
        UIManager.put("Button.select", new Color(155, 193, 232));
        
        // 在"另存为"对话框中更改按钮的边框
        UIManager.put("ToggleButton.border", HelperUi.BORDER_BLU);

        // 菜单无粗体+圆角
        UIManager.put("Menu.font", HelperUi.FONT_SEGOE);
        UIManager.put("Menu.selectionBackground", HelperUi.COLOR_FOCUS_GAINED);
        UIManager.put("Menu.borderPainted", false);
        UIManager.put("PopupMenu.font", HelperUi.FONT_SEGOE);
        UIManager.put("RadioButtonMenuItem.selectionBackground", HelperUi.COLOR_FOCUS_GAINED);
        UIManager.put("RadioButtonMenuItem.font", HelperUi.FONT_SEGOE);
        UIManager.put("RadioButtonMenuItem.borderPainted", false);
        UIManager.put("MenuItem.selectionBackground", HelperUi.COLOR_FOCUS_GAINED);
        UIManager.put("MenuItem.font", HelperUi.FONT_SEGOE);
        UIManager.put("MenuItem.borderPainted", false);
        UIManager.put("MenuItem.disabledAreNavigable", Boolean.TRUE);
        
        UIManager.put("CheckBoxMenuItem.selectionBackground", HelperUi.COLOR_FOCUS_GAINED);
        UIManager.put("CheckBoxMenuItem.font", HelperUi.FONT_SEGOE);
        UIManager.put("CheckBoxMenuItem.borderPainted", false);
        UIManager.put("CheckBoxMenuItem.checkIcon", new CheckBoxIcon());

        // 自定义 tab
        UIManager.put("TabbedPane.contentAreaColor", HelperUi.FONT_UBUNTU_MONO);
        UIManager.put("TabbedPane.font", HelperUi.FONT_SEGOE);
        // 当前选项卡面板的边距
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        // 标签上方的边距
        UIManager.put("TabbedPane.tabAreaInsets", new Insets(3, 2, 0, 2));
        // 标签名称周围的边距
        UIManager.put("TabbedPane.tabInsets", new Insets(2, 3 + 5, 2, 3));
        // 未选中的选项卡边框更亮
        UIManager.put("TabbedPane.darkShadow", new Color(190,198,205));

        UIManager.put("Button.font", HelperUi.FONT_SEGOE);
        UIManager.put("CheckBox.font", HelperUi.FONT_SEGOE);
        UIManager.put("RadioButton.font", HelperUi.FONT_SEGOE);
        UIManager.put("TitledBorder.font", HelperUi.FONT_SEGOE);

        UIManager.put("Spinner.arrowButtonBorder", HelperUi.BORDER_BLU);
        UIManager.put("Spinner.border", BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(HelperUi.COLOR_BLU),
            BorderFactory.createMatteBorder(2,2,2,2, Color.WHITE)
        ));
        
        UIManager.put("FileChooser.listFont", HelperUi.FONT_SEGOE);
        UIManager.put("FileChooser.listViewBorder", BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(HelperUi.COLOR_BLU),
            BorderFactory.createMatteBorder(2,2,2,2, Color.WHITE)
        ));

        // 自定义文本组件
        // 管理页面
        UIManager.put("TextPane.selectionBackground", HelperUi.COLOR_FOCUS_GAINED);
        UIManager.put("TextPane.font", HelperUi.FONT_UBUNTU_MONO);
        UIManager.put("TextPane.selectionBackground", HelperUi.COLOR_FOCUS_GAINED);
        
        UIManager.put("TextField.border", HelperUi.BORDER_BLU);
        UIManager.put("TextField.selectionBackground", HelperUi.COLOR_FOCUS_GAINED);
        
        UIManager.put("EditorPane.selectionBackground", HelperUi.COLOR_FOCUS_GAINED);
        
        UIManager.put("TextArea.selectionBackground", HelperUi.COLOR_FOCUS_GAINED);
        UIManager.put("TextArea.font", HelperUi.FONT_UBUNTU_MONO);

        // 自定义 Label
        UIManager.put("Label.font", HelperUi.FONT_SEGOE);
        UIManager.put("Label.selectionBackground", HelperUi.COLOR_FOCUS_GAINED);

        // 自定义 table
        UIManager.put("Table.font", HelperUi.FONT_SEGOE);
        UIManager.put("TableHeader.font", HelperUi.FONT_SEGOE);
        UIManager.put("Table.selectionBackground", HelperUi.COLOR_FOCUS_GAINED);
        UIManager.put("Table.focusCellHighlightBorder",
            BorderFactory.createCompoundBorder(
                new AbstractBorder() {
                    @Override
                    public void paintBorder(Component comp, Graphics g, int x, int y, int w, int h) {
                        Graphics2D gg = (Graphics2D) g;
                        gg.setColor(Color.GRAY);
                        gg.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1}, 0));
                        try {
                            gg.drawRect(x, y, w - 1, h - 1);
                        } catch (InternalError e) {
                            LOGGER.error(e, e);
                        }
                    }
                },
                BorderFactory.createEmptyBorder(0, 1, 0, 0)
            )
        );
        
        // 自定义树结构
        UIManager.put("Tree.expandedIcon", new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/expanded.png")));
        UIManager.put("Tree.collapsedIcon", new ImageIcon(HelperUi.class.getClassLoader().getResource("swing/images/icons/collapsed.png")));
        UIManager.put("Tree.lineTypeDashed", true);
        // 树节点没有默认图标
        UIManager.put("Tree.leafIcon", new ImageIcon());
        UIManager.put("Tree.openIcon", new ImageIcon());
        UIManager.put("Tree.closedIcon", new ImageIcon());

        // 自定义进度条
        UIManager.put("ProgressBar.border", BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(3, 0, 4, 0),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createLineBorder(Color.WHITE)
            )
        ));
        UIManager.put("ProgressBar.foreground", new Color(136, 183, 104));
        UIManager.put("ProgressBar.background", UIManager.get("Tree.background"));

        // 自定义下拉框
        UIManager.put("ComboBox.font", HelperUi.FONT_SEGOE);
        UIManager.put("ComboBox.selectionBackground", HelperUi.COLOR_FOCUS_GAINED);
        // 使用ColorUIResource保留箭头的背景色
        UIManager.put("ComboBox.background", new ColorUIResource(Color.WHITE));
        UIManager.put("ComboBox.border", HelperUi.BORDER_BLU);
        UIManager.put("ComboBoxUI", CustomBasicComboBoxUI.class.getName());
        
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        
        UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
    }

    /**
     * 	应用程序窗口的图标。
     */
    public static List<Image> getIcons() {
        List<Image> images = new ArrayList<>();
        try {
            images.add(ImageIO.read(HelperUi.URL_ICON_128));
            images.add(ImageIO.read(HelperUi.URL_ICON_96));
            images.add(ImageIO.read(HelperUi.URL_ICON_32));
            images.add(ImageIO.read(HelperUi.URL_ICON_16));
        } catch (NoClassDefFoundError | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return images;
    }
    
}
