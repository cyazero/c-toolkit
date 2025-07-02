package org.example;

import org.example.cli.ToolCLI;
import org.example.gui.ToolGUI;

/**
 * Created on 2025/07/01
 */
public class MainLauncher {
    public static void main(String[] args) {
        // 默认没有参数时启动GUI
        if (args.length == 0) {
            ToolGUI.main(new String[]{});
        }
        // 有参数且第一个参数为--cli时启动命令行界面
        else if (args[0].equals("--cli") || args[0].equals("-c")) {
            ToolCLI.main(removeFirstArg(args));
        }
        // 其他情况启动GUI（带参数可能表示要执行特定命令）
        else {
            // 可以在这里添加从GUI执行特定命令的逻辑
            ToolGUI.main(new String[]{});
        }
    }

    private static String[] removeFirstArg(String[] args) {
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        return newArgs;
    }
}