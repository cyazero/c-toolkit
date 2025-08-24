package org.example.tools;

import org.example.annotation.Tool;
import org.example.annotation.ToolMethod;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Created on 2025/07/25
 */
@Tool(command = "base64", name = "base64加密", description = "对字符串base64加密", parameters = {"decode:明文", "encode:密文"})
public class Base64Tool {
    @ToolMethod(description = "计算分表位置")
    public String execute(Map<String, String> parameters) {
        String encode = parameters.get("encode");
        String decode = parameters.get("decode");
        if (encode != null && !encode.isEmpty()) {
            return decodeFromBase64(encode);
        } else if (decode != null && !decode.isEmpty()) {
            return encodeToBase64(decode);
        } else {
            return "错误: 必须提供明文或密文";
        }
    }

    public static String encodeToBase64(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] encodedBytes = Base64.getEncoder().encode(bytes);
        return new String(encodedBytes, StandardCharsets.UTF_8);
    }

    public static String decodeFromBase64(String base64Input) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Input);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}