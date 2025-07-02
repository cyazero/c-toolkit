package org.example.cli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.example.core.ToolRegistry;
import org.example.core.ToolRegistry.ToolInfo;
import org.example.gui.ToolGUI;


/**
 * 命令行工具接口
 * Created on 2025/07/01
 */
public class ToolCLI {

    public static void main(String[] args) {
        // 初始化工具注册
        ToolRegistry.initialize("org.example.tools");

        // 没有参数时显示帮助
        if (args.length == 0) {
            printHelp();
            return;
        }

        // 如果第一个参数是--gui或-g，启动GUI
        if ("--gui".equals(args[0]) || "-g".equals(args[0])) {
            // 启动GUI（不带参数）
            ToolGUI.main(new String[]{});
            return;
        }

        String command = args[0];

        // 检查命令是否存在
        if (!ToolRegistry.getAllToolCommands().contains(command)) {
            System.out.println("未找到命令: " + command);
            printAvailableCommands();
            return;
        }

        // 解析参数
        Map<String, String> params = parseParams(Arrays.copyOfRange(args, 1, args.length));

        // 执行工具
        String result = ToolRegistry.executeTool(command, params);
        System.out.println(result);
    }

    private static void printHelp() {
        System.out.println("c-toolkit命令行界面");
        System.out.println("使用方法: java ToolCLI <command> [参数]");
        System.out.println();
        System.out.println("可用命令:");

        Set<String> commands = ToolRegistry.getAllToolCommands();
        for (String command : commands) {
            ToolInfo info = ToolRegistry.getToolInfo(command);
            System.out.println("  " + command + " - " + info.name);
            System.out.println("      描述: " + info.description);

            if (info.parameters.length > 0) {
                System.out.println("      参数:");
                for (String param : info.parameters) {
                    String[] parts = param.split(":", 2);
                    String paramName = parts[0];
                    String paramDesc = parts.length > 1 ? parts[1] : "";
                    System.out.println("        " + paramName + ": " + paramDesc);
                }
            }
            System.out.println();
        }
    }

    private static void printAvailableCommands() {
        System.out.println("可用命令: " + String.join(", ", ToolRegistry.getAllToolCommands()));
    }

    private static Map<String, String> parseParams(String[] args) {
        Map<String, String> params = new HashMap<>();

        for (String arg : args) {
            if (arg.contains("=")) {
                String[] parts = arg.split("=", 2);
                if (parts.length == 2) {
                    params.put(parts[0], parts[1]);
                } else {
                    params.put(parts[0], "");
                }
            } else {
                // 没有"="的参数作为值存储在特殊键下
                params.put("arg" + params.size(), arg);
            }
        }

        return params;
    }
}