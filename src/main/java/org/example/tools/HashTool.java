package org.example.tools;

import org.example.annotation.Tool;
import org.example.annotation.ToolMethod;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created on 2025/09/16
 */
@Tool(command = "hash", name = "哈希(md5,sha256...)", description = "对字符串哈希", parameters = {"string:原始字符串"})
public class HashTool {

    @ToolMethod
    public static String execute(Map<String, String> parameters) throws NoSuchAlgorithmException {
        String string = parameters.get("string");
        if (string != null && !string.isEmpty()) {
            return "md5: " + generateHash(string, "MD5") + "\nsha256: " + generateHash(string, "SHA-256");
        } else {
            return "错误: 必须提供原始字符串";
        }
    }

    /**
     * 生成字符串的哈希值
     * @param input 原始字符串
     * @param algorithm 算法名称（如 "MD5", "SHA-256"）
     * @return 十六进制格式的哈希值
     * @throws NoSuchAlgorithmException 如果算法不支持
     */
    private static String generateHash(String input, String algorithm) throws NoSuchAlgorithmException {
        if (input == null || algorithm == null) {
            throw new IllegalArgumentException("Input and algorithm must not be null");
        }

        // 获取 MessageDigest 实例
        MessageDigest digest = MessageDigest.getInstance(algorithm);

        // 计算哈希值
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        // 将字节数组转换为十六进制字符串
        return bytesToHex(hashBytes);
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0'); // 补齐单字符前的0
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }


}
