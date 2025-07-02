package org.example.core;

import org.example.annotation.Tool;
import org.example.annotation.ToolMethod;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 工具注册中心
 * Created on 2025/07/01
 */
public class ToolRegistry {
    private static final Map<String, ToolExecutor> toolMap = new HashMap<>();
    private static final Map<String, ToolInfo> toolInfoMap = new HashMap<>();

    /**
     * 工具信息类
     */
    public static class ToolInfo {
        public final String name;
        public final String description;
        public final String[] parameters;

        public ToolInfo(String name, String description, String[] parameters) {
            this.name = name;
            this.description = description;
            this.parameters = parameters;
        }
    }

    /**
     * 工具执行器接口
     */
    @FunctionalInterface
    public interface ToolExecutor {
        String execute(Map<String, String> parameters) throws Exception;
    }

    /**
     * 初始化工具注册
     * @param basePackages 要扫描的包
     */
    public static void initialize(String... basePackages) {
        // 加载SPI扩展（可选）
        loadSpiTools();

        // 扫描注解工具
        for (String pkg : basePackages) {
            scanPackage(pkg);
        }
    }

    /**
     * 扫描包并注册工具
     */
    private static void scanPackage(String packageName) {
        List<Class<?>> classes = ClasspathScanner.getClassesForPackage(packageName);
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Tool.class)) {
                registerAnnotatedClass(clazz);
            }
        }
    }

    /**
     * 注册带有@Tool注解的类
     */
    private static void registerAnnotatedClass(Class<?> clazz) {
        Tool toolAnnotation = clazz.getAnnotation(Tool.class);
        String command = toolAnnotation.command();

        // 查找执行方法
        Method executeMethod = findExecuteMethod(clazz);
        if (executeMethod == null) {
            System.err.println("警告: 类 " + clazz.getName() + " 没有合适的执行方法");
            return;
        }

        // 创建工具执行器
        ToolExecutor executor = createToolExecutor(clazz, executeMethod);

        // 注册工具
        registerTool(command, executor, toolAnnotation);
        System.out.println("注册工具: " + toolAnnotation.name() + " (" + command + ")");
    }

    /**
     * 查找执行方法
     */
    private static Method findExecuteMethod(Class<?> clazz) {
        // 查找带有@ToolMethod注解的方法
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(ToolMethod.class)) {
                return method;
            }
        }

        // 查找标准的execute方法
        try {
            return clazz.getMethod("execute", Map.class);
        } catch (NoSuchMethodException e) {
            // 尝试其他可能的方法
            for (Method method : clazz.getMethods()) {
                if ("execute".equals(method.getName()) &&
                        method.getParameterCount() == 1 &&
                        Map.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * 创建工具执行器
     */
    private static ToolExecutor createToolExecutor(Class<?> clazz, Method method) {
        // 如果方法是静态的，不需要实例化
        if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
            return parameters -> {
                try {
                    return (String) method.invoke(null, parameters);
                } catch (Exception e) {
                    throw new ToolExecutionException("静态方法调用失败", e);
                }
            };
        }

        // 实例方法需要创建对象
        return parameters -> {
            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                return (String) method.invoke(instance, parameters);
            } catch (Exception e) {
                throw new ToolExecutionException("方法调用失败", e);
            }
        };
    }

    /**
     * 注册工具
     */
    public static void registerTool(String command, ToolExecutor executor, Tool annotation) {
        if (toolMap.containsKey(command)) {
            System.err.println("警告: 重复的工具命令 '" + command + "', 将覆盖已有工具");
        }
        toolMap.put(command, executor);
        toolInfoMap.put(command, new ToolInfo(
                annotation.name(),
                annotation.description(),
                annotation.parameters()
        ));
    }

    /**
     * 执行工具
     */
    public static String executeTool(String command, Map<String, String> parameters) {
        ToolExecutor executor = toolMap.get(command);
        if (executor == null) {
            return "未找到命令: " + command;
        }

        try {
            return executor.execute(parameters);
        } catch (Exception e) {
            return "执行错误: " + e.getMessage();
        }
    }

    /**
     * 获取所有工具命令
     */
    public static Set<String> getAllToolCommands() {
        return toolMap.keySet();
    }

    /**
     * 获取工具信息
     */
    public static ToolInfo getToolInfo(String command) {
        return toolInfoMap.get(command);
    }

    /**
     * 加载SPI扩展工具（可选）
     */
    private static void loadSpiTools() {
        // 使用Java标准SPI加载扩展
        ServiceLoader<ToolProvider> loader = ServiceLoader.load(ToolProvider.class);
        for (ToolProvider provider : loader) {
            registerTool(
                    provider.getCommand(),
                    provider::execute,
                    new Tool() {
                        @Override
                        public String command() {
                            return provider.getCommand();
                        }

                        @Override
                        public String name() {
                            return provider.getName();
                        }

                        @Override
                        public String description() {
                            return provider.getDescription();
                        }

                        @Override
                        public String[] parameters() {
                            return provider.getParameters();
                        }

                        @Override
                        public Class<? extends java.lang.annotation.Annotation> annotationType() {
                            return Tool.class;
                        }
                    }
            );
            System.out.println("加载SPI工具: " + provider.getName());
        }
    }

    /**
     * 自定义执行异常
     */
    private static class ToolExecutionException extends RuntimeException {
        public ToolExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * SPI工具提供者接口
     */
    public interface ToolProvider {
        String getCommand();
        String getName();
        String getDescription();
        String[] getParameters();
        String execute(Map<String, String> parameters) throws Exception;
    }
}