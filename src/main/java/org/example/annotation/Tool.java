package org.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on 2025/07/01
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Tool {

    /**
     * 工具名称
     * @return
     */
    String name();

    /**
     * 工具描述
     * @return
     */
    String description();

    /**
     * 工具命令（唯一标识）
     * @return
     */
    String command();

    /**
     * 参数列表（格式：参数名:描述）
     * @return
     */
    String[] parameters() default {};
}
