package org.example.tools;

import org.example.annotation.Tool;
import org.example.annotation.ToolMethod;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2025/08/21
 */
@Tool(command = "Unicode", name = "Unicode加密", description = "对字符串Unicode加密", parameters = {"decode:明文", "encode:密文"})
public class UnicodeTool {
    @ToolMethod(description = "计算分表位置")
    public String execute(Map<String, String> parameters) {
        String encode = parameters.get("encode");
        String decode = parameters.get("decode");
        if (encode != null && !encode.isEmpty()) {
            return decodeUnicode(encode);
        } else if (decode != null && !decode.isEmpty()) {
            return encodeUnicode(decode);
        } else {
            return "错误: 必须提供明文或密文";
        }
    }

    /**
     * 将字符串编码为Unicode转义序列 * @param input 原始字符串 * @return Unicode编码后的字符串（格式：\u57fa）
     */
    public static String encodeUnicode(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            // ASCII字符直接追加（0-127）
            if (c <= 127) {
                sb.append(c);
            }
            // 非ASCII字符转为Unicode转义序列
            else {
                sb.append("\\u").append(String.format("%04x", (int) c).toUpperCase());
            }
        }
        return sb.toString();
    }

    /**
     * 将Unicode转义序列解码为原始字符串 * @param input Unicode编码字符串 * @return 解码后的原始字符串
     */
    public static String decodeUnicode(String input) {
        Pattern pattern = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            // 将十六进制代码点转为字符
            char decodedChar = (char) Integer.parseInt(matcher.group(1), 16);
            matcher.appendReplacement(sb, Character.toString(decodedChar));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}