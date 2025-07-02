package org.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.example.core.ToolRegistry;
import org.example.core.ToolRegistry.ToolInfo;

/**
 * 工具包图形界面
 * Created on 2025/07/01
 */
public class ToolGUI extends JFrame {

    private final JComboBox<String> toolSelector;
    private final JTextArea resultArea;
    private final Map<String, JTextField> inputFields = new HashMap<>();
    private final JPanel inputPanel;

    public ToolGUI() {
        this(true);
    }

    private ToolGUI(boolean autoShow) {
        super("c-toolkit");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // 初始化工具注册
        ToolRegistry.initialize("org.example.tools");

        // 工具选择器
        toolSelector = new JComboBox<>();
        Set<String> commands = ToolRegistry.getAllToolCommands();
        for (String command : commands) {
            ToolInfo info = ToolRegistry.getToolInfo(command);
            toolSelector.addItem(info.name + " (" + command + ")");
        }

        toolSelector.addActionListener(this::onToolSelected);

        // 输入面板
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(0, 2, 5, 5));

        // 结果区域设置 - 增强滚动功能
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setLineWrap(true); // 启用自动换行
        resultArea.setWrapStyleWord(true); // 按单词换行
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        resultScrollPane.setPreferredSize(new Dimension(0, 200)); // 设置初始高度

        // 创建主内容面板（使用弹性布局）
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(inputPanel);
        contentPanel.add(Box.createVerticalStrut(10)); // 添加间距
        contentPanel.add(resultScrollPane);

        // 执行按钮
        JButton executeButton = new JButton("执行");
        executeButton.addActionListener(this::executeTool);

        // 布局
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("选择工具:"), BorderLayout.WEST);
        topPanel.add(toolSelector, BorderLayout.CENTER);
        topPanel.add(executeButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        // 初始化第一个工具
        if (toolSelector.getItemCount() > 0) {
            toolSelector.setSelectedIndex(0);
            onToolSelected(null);
        }

        setLocationRelativeTo(null);

        // 不自动显示窗口
        if (!autoShow) {
            setVisible(false);
        }
    }

    private void onToolSelected(ActionEvent e) {
        // 清除旧输入字段
        inputPanel.removeAll();
        inputFields.clear();

        String selected = (String) toolSelector.getSelectedItem();
        if (selected == null) return;

        // 提取命令
        String command = selected.substring(selected.lastIndexOf('(') + 1, selected.length() - 1);
        ToolInfo info = ToolRegistry.getToolInfo(command);

        if (info == null) return;

        // 创建输入字段
        for (String param : info.parameters) {
            String[] parts = param.split(":", 2);
            String paramName = parts[0];
            String paramDesc = parts.length > 1 ? parts[1] : "";

            JLabel label = new JLabel(paramName + ":" + paramDesc);
            JTextField textField = new JTextField();

            inputPanel.add(label);
            inputPanel.add(textField);
            inputFields.put(paramName, textField);
        }

        // 更新UI
        inputPanel.revalidate();
        inputPanel.repaint();
    }

    private void executeTool(ActionEvent e) {
        String selected = (String) toolSelector.getSelectedItem();
        if (selected == null) return;

        // 提取命令
        String command = selected.substring(selected.lastIndexOf('(') + 1, selected.length() - 1);

        // 收集参数
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, JTextField> entry : inputFields.entrySet()) {
            params.put(entry.getKey(), entry.getValue().getText());
        }

        // 执行工具
        String result = ToolRegistry.executeTool(command, params);
        resultArea.setText(result);
    }

    /**
     * 从命令行执行特定工具
     * @param command 要执行的命令
     * @param params 参数字符串数组（格式: key=value）
     */
    public static void executeCommandFromCli(String command, String[] params) {
        // 创建临时GUI但不显示
        ToolGUI gui = new ToolGUI(false);

        // 选择指定工具
        gui.selectCommand(command);

        // 设置参数
        Map<String, String> paramsMap = new HashMap<>();
        for (String param : params) {
            String[] parts = param.split("=", 2);
            if (parts.length == 2) {
                paramsMap.put(parts[0], parts[1]);
            }
        }

        // 执行工具并显示结果
        String result = gui.executeToolInternal(command, paramsMap);
        JOptionPane.showMessageDialog(null, result, "执行结果", JOptionPane.INFORMATION_MESSAGE);

        // 现在显示完整GUI
        gui.setVisible(true);
    }

    /**
     * 选择指定命令
     */
    private void selectCommand(String command) {
        for (int i = 0; i < toolSelector.getItemCount(); i++) {
            String item = (String) toolSelector.getItemAt(i);
            if (item.contains("(" + command + ")")) {
                toolSelector.setSelectedIndex(i);
                onToolSelected(null);
                return;
            }
        }
        System.err.println("命令未找到: " + command);
    }

    // 内部执行工具方法
    private String executeToolInternal(String command, Map<String, String> params) {
        // 设置结果区域
        resultArea.setText("执行中...");

        // 收集参数
        for (Map.Entry<String, JTextField> entry : inputFields.entrySet()) {
            if (params.containsKey(entry.getKey())) {
                entry.getValue().setText(params.get(entry.getKey()));
            }
        }

        // 执行工具
        String result = ToolRegistry.executeTool(command, params);
        resultArea.setText(result);

        return result;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ToolGUI gui = new ToolGUI();
            gui.setVisible(true);
        });
    }
}