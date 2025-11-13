package org.example.gui;

import org.example.core.ToolRegistry;
import org.example.core.ToolRegistry.ToolInfo;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 工具包图形界面
 * Created on 2025/07/01
 */
public class ToolGUI extends JFrame {

    private final JList<String> toolList;
    private final DefaultListModel<String> listModel;
    private final JTextPane resultPane;
    private final Map<String, JTextField> inputFields = new HashMap<>();
    private final JPanel inputPanel;
    private final Map<String, String> commandMap = new HashMap<>();
    private final JSplitPane rightSplitPane;

    // 字体定义（带回退机制）
    private final Font yaheiFont;
    private final Font consolasFont;
    private final Font yaheiBoldFont;
    private final Font yaheiSmallFont;

    public ToolGUI() {
        this(true);
    }

    private ToolGUI(boolean autoShow) {
        super("c-toolkit");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // 初始化字体（带回退机制）
        yaheiFont = getFontWithFallback("微软雅黑", "Microsoft YaHei", "SimSun", "宋体", Font.PLAIN, 12);
        consolasFont = getFontWithFallback("Consolas", "Monaco", "Courier New", "Monospaced", Font.PLAIN, 12);
        yaheiBoldFont = getFontWithFallback("微软雅黑", "Microsoft YaHei", "SimSun", "宋体", Font.BOLD, 12);
        yaheiSmallFont = getFontWithFallback("微软雅黑", "Microsoft YaHei", "SimSun", "宋体", Font.PLAIN, 10);

        // 初始化工具注册
        ToolRegistry.initialize("org.example.tools");

        // 创建主布局 - 左右分割
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(250);
        mainSplitPane.setResizeWeight(0.3);
        mainSplitPane.setOneTouchExpandable(true);

        // 左侧工具列表
        listModel = new DefaultListModel<>();
        toolList = new JList<>(listModel);
        toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        toolList.setFont(yaheiFont);

        toolList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onToolSelected();
            }
        });

        JScrollPane listScrollPane = new JScrollPane(toolList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("工具列表"));
        mainSplitPane.setLeftComponent(listScrollPane);

        // 右侧内容面板
        rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setResizeWeight(0.5);
        rightSplitPane.setOneTouchExpandable(true);

        // 输入面板
        inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 结果区域 - 使用JTextPane支持混合字体
        resultPane = new JTextPane();
        resultPane.setEditable(false);
        resultPane.setContentType("text/plain");
        JScrollPane resultScrollPane = new JScrollPane(resultPane);
        resultScrollPane.setBorder(BorderFactory.createTitledBorder("执行结果"));
        resultScrollPane.setPreferredSize(new Dimension(0, 300));

        // 执行按钮
        JButton executeButton = new JButton("执行工具");
        executeButton.setFont(yaheiBoldFont);
        executeButton.addActionListener(this::executeTool);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.add(executeButton);

        // 参数区域容器
        JPanel paramContainer = new JPanel(new BorderLayout());
        JScrollPane inputScrollPane = new JScrollPane(inputPanel);
        inputScrollPane.setBorder(BorderFactory.createTitledBorder("参数设置"));
        inputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        inputScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        paramContainer.add(inputScrollPane, BorderLayout.CENTER);
        paramContainer.add(buttonPanel, BorderLayout.SOUTH);

        rightSplitPane.setTopComponent(paramContainer);
        rightSplitPane.setBottomComponent(resultScrollPane);

        mainSplitPane.setRightComponent(rightSplitPane);

        add(mainSplitPane, BorderLayout.CENTER);

        // 初始化工具列表
        initializeToolList();

        // 不自动显示窗口
        if (!autoShow) {
            setVisible(false);
        }
    }

    /**
     * 获取字体（带回退机制）
     * @param preferredFont 首选字体
     * @param fallback1 回退字体1
     * @param fallback2 回退字体2
     * @param fallback3 回退字体3
     * @param style 字体样式
     * @param size 字体大小
     * @return 可用的字体
     */
    private Font getFontWithFallback(String preferredFont, String fallback1, String fallback2, String fallback3, int style, int size) {
        // 尝试首选字体
        Font font = new Font(preferredFont, style, size);
        if (font.getFamily().equals(preferredFont)) {
            return font;
        }

        // 尝试回退字体
        font = new Font(fallback1, style, size);
        if (font.getFamily().equals(fallback1)) {
            return font;
        }

        font = new Font(fallback2, style, size);
        if (font.getFamily().equals(fallback2)) {
            return font;
        }

        font = new Font(fallback3, style, size);
        if (font.getFamily().equals(fallback3)) {
            return font;
        }

        // 所有字体都不可用，使用默认字体
        return new Font(Font.SANS_SERIF, style, size);
    }

    private void initializeToolList() {
        Set<String> commands = ToolRegistry.getAllToolCommands();
        for (String command : commands) {
            ToolInfo info = ToolRegistry.getToolInfo(command);
            if (info != null) {
                String displayName = info.name + " (" + command + ")";
                listModel.addElement(displayName);
                commandMap.put(displayName, command);
            }
        }

        if (!listModel.isEmpty()) {
            toolList.setSelectedIndex(0);
        }
    }

    private void onToolSelected() {
        // 清除旧输入字段
        inputPanel.removeAll();
        inputFields.clear();

        String selected = toolList.getSelectedValue();
        if (selected == null) return;

        String command = commandMap.get(selected);
        if (command == null) return;

        ToolInfo info = ToolRegistry.getToolInfo(command);
        if (info == null) return;

        // 创建参数输入字段，包含完整描述
        for (String param : info.parameters) {
            String[] parts = param.split(":", 2);
            String paramName = parts[0];
            String paramDesc = parts.length > 1 ? parts[1] : "";

            // 创建参数面板
            JPanel paramPanel = new JPanel(new GridBagLayout());
            paramPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            GridBagConstraints gbc = new GridBagConstraints();

            // 参数名标签
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets = new Insets(0, 0, 2, 5);
            JLabel nameLabel = new JLabel(paramName + ":");
            nameLabel.setFont(yaheiBoldFont);
            paramPanel.add(nameLabel, gbc);

            // 输入框
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 2, 0);
            JTextField textField = new JTextField();
            textField.setPreferredSize(new Dimension(200, 25));
            textField.setMinimumSize(new Dimension(150, 25));
            textField.setFont(yaheiFont); // 输入框使用yahei
            textField.setToolTipText(paramDesc);
            paramPanel.add(textField, gbc);

            // 参数描述
            if (!paramDesc.isEmpty()) {
                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.gridwidth = 2;
                gbc.weightx = 1.0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(0, 0, 0, 0);

                JTextArea descArea = new JTextArea(paramDesc);
                descArea.setEditable(false);
                descArea.setLineWrap(true);
                descArea.setWrapStyleWord(true);
                descArea.setBackground(paramPanel.getBackground());
                descArea.setFont(yaheiSmallFont);
                descArea.setForeground(Color.GRAY);
                descArea.setBorder(BorderFactory.createEmptyBorder(2, 0, 5, 0));
                descArea.setFocusable(false);

                paramPanel.add(descArea, gbc);
            }

            inputPanel.add(paramPanel);
            inputPanel.add(Box.createVerticalStrut(5));

            inputFields.put(paramName, textField);
        }

        // 如果没有参数，显示提示
        if (info.parameters.length == 0) {
            JLabel noParamsLabel = new JLabel("此工具无需参数", JLabel.CENTER);
            noParamsLabel.setFont(yaheiFont);
            noParamsLabel.setForeground(Color.GRAY);
            noParamsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            inputPanel.add(noParamsLabel);
        }

        // 添加弹性空间使内容靠上
        inputPanel.add(Box.createVerticalGlue());

        // 更新UI
        inputPanel.revalidate();
        inputPanel.repaint();

        // 调整分割条位置
        SwingUtilities.invokeLater(() -> rightSplitPane.setDividerLocation(0.4));
    }

    private void executeTool(ActionEvent e) {
        // 清空结果区域
        clearResultPane();

        String selected = toolList.getSelectedValue();
        if (selected == null) {
            appendToResultPane("请先选择一个工具", Color.RED, yaheiFont);
            return;
        }

        String command = commandMap.get(selected);
        if (command == null) {
            appendToResultPane("未找到对应工具", Color.RED, yaheiFont);
            return;
        }

        // 收集参数
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, JTextField> entry : inputFields.entrySet()) {
            params.put(entry.getKey(), entry.getValue().getText());
        }

        // 执行工具
        try {
            // 不再显示"执行中..."提示，直接显示结果
            String result = ToolRegistry.executeTool(command, params);
            appendToResultPane(result, Color.BLACK, null); // 使用智能字体选择
        } catch (Exception ex) {
            appendToResultPane("执行错误: " + ex.getMessage(), Color.RED, yaheiFont);
            ex.printStackTrace();
        }
    }

    /**
     * 向结果面板追加文本，支持中英文不同字体
     */
    private void appendToResultPane(String text, Color color, Font specificFont) {
        StyledDocument doc = resultPane.getStyledDocument();

        try {
            // 移动到文档末尾
            int offset = doc.getLength();
            doc.insertString(offset, text, null);

            // 创建样式
            Style style = resultPane.addStyle("CustomStyle", null);
            StyleConstants.setForeground(style, color);

            // 如果指定了字体，直接使用
            if (specificFont != null) {
                StyleConstants.setFontFamily(style, specificFont.getFamily());
                StyleConstants.setFontSize(style, specificFont.getSize());
                doc.setCharacterAttributes(offset, text.length(), style, false);
            } else {
                // 智能字体选择：中文字体用微软雅黑，英文字体用Consolas
                applyMixedFont(doc, offset, text.length(), style);
            }

            // 滚动到末尾
            resultPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 应用混合字体：中文用微软雅黑，英文用Consolas
     */
    private void applyMixedFont(StyledDocument doc, int offset, int length, Style baseStyle) {
        String text;
        try {
            text = doc.getText(offset, length);
        } catch (BadLocationException ex) {
            return;
        }

        // 遍历每个字符，根据字符类型应用不同字体
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            Style charStyle = resultPane.addStyle("CharStyle" + i, baseStyle);

            // 判断字符类型：中文或其他CJK字符使用微软雅黑，其他使用Consolas
            if (isCJKCharacter(c)) {
                StyleConstants.setFontFamily(charStyle, yaheiFont.getFamily());
                StyleConstants.setFontSize(charStyle, yaheiFont.getSize());
            } else {
                StyleConstants.setFontFamily(charStyle, consolasFont.getFamily());
                StyleConstants.setFontSize(charStyle, consolasFont.getSize());
            }

            doc.setCharacterAttributes(offset + i, 1, charStyle, false);
        }
    }

    /**
     * 判断字符是否为CJK字符（中文、日文、韩文等）
     */
    private boolean isCJKCharacter(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || block == Character.UnicodeBlock.HIRAGANA
                || block == Character.UnicodeBlock.KATAKANA
                || block == Character.UnicodeBlock.HANGUL_SYLLABLES
                || block == Character.UnicodeBlock.HANGUL_JAMO
                || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO;
    }

    /**
     * 清空结果面板
     */
    private void clearResultPane() {
        resultPane.setText("");
    }

    /**
     * 从命令行执行特定工具
     */
    public static void executeCommandFromCli(String command, String[] params) {
        ToolGUI gui = new ToolGUI(false);
        gui.selectCommand(command);

        Map<String, String> paramsMap = new HashMap<>();
        for (String param : params) {
            String[] parts = param.split("=", 2);
            if (parts.length == 2) {
                paramsMap.put(parts[0], parts[1]);
            }
        }

        String result = gui.executeToolInternal(command, paramsMap);
        JOptionPane.showMessageDialog(null, result, "执行结果", JOptionPane.INFORMATION_MESSAGE);
        gui.setVisible(true);
    }

    /**
     * 选择指定命令
     */
    private void selectCommand(String command) {
        for (int i = 0; i < listModel.size(); i++) {
            String item = listModel.getElementAt(i);
            if (item.contains("(" + command + ")")) {
                toolList.setSelectedIndex(i);
                onToolSelected();
                return;
            }
        }
        System.err.println("命令未找到: " + command);
    }

    // 内部执行工具方法
    private String executeToolInternal(String command, Map<String, String> params) {
        clearResultPane();

        for (Map.Entry<String, JTextField> entry : inputFields.entrySet()) {
            if (params.containsKey(entry.getKey())) {
                entry.getValue().setText(params.get(entry.getKey()));
            }
        }

        String result = ToolRegistry.executeTool(command, params);
        appendToResultPane(result, Color.BLACK, null);
        return result;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            ToolGUI gui = new ToolGUI();
            gui.setVisible(true);
        });
    }
}