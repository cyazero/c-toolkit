package org.example.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类路径扫描器 - 纯Java实现
 * Created on 2025/07/01
 */
public class ClasspathScanner {

    /**
     * 获取指定包下的所有类
     * @param packageName 包名
     * @return 类列表
     */
    public static List<Class<?>> getClassesForPackage(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(path);

            List<File> dirs = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                URI uri = resource.toURI();

                if ("file".equals(uri.getScheme())) {
                    dirs.add(new File(uri.getPath()));
                } else if ("jar".equals(uri.getScheme())) {
                    processJarFile(uri, path, packageName, classes);
                }
            }

            // 处理文件系统的类
            for (File directory : dirs) {
                findClassesInDirectory(packageName, directory, classes);
            }
        } catch (IOException | URISyntaxException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return classes;
    }

    /**
     * 处理JAR文件中的类
     */
    private static void processJarFile(URI jarUri, String path, String packageName,
                                       List<Class<?>> classes)
            throws IOException, ClassNotFoundException {

        String jarPath = jarUri.toString().split("!")[0].substring("jar:file:".length());
        JarFile jar = new JarFile(new File(jarPath));
        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();

            // 检查是否在指定包路径下
            if (entryName.startsWith(path) && entryName.endsWith(".class")) {
                String className = entryName.substring(0, entryName.length() - 6)
                        .replace('/', '.');
                try {
                    classes.add(Class.forName(className));
                } catch (NoClassDefFoundError e) {
                    // 忽略无法加载的类
                }
            }
        }
        jar.close();
    }

    /**
     * 在目录中查找类
     */
    private static void findClassesInDirectory(String packageName, File directory,
                                               List<Class<?>> classes)
            throws ClassNotFoundException {
        if (!directory.exists()) return;

        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // 递归子目录
                String subPackage = packageName + "." + file.getName();
                findClassesInDirectory(subPackage, file, classes);
            } else if (file.getName().endsWith(".class")) {
                // 将文件路径转换为类名
                String className = packageName + '.' +
                        file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Class.forName(className));
                } catch (NoClassDefFoundError e) {
                    // 忽略无法加载的类
                }
            }
        }
    }
}