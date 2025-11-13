package org.example.tools;

import org.example.annotation.Tool;
import org.example.annotation.ToolMethod;

import java.security.*;
import java.util.Base64;
import java.util.Map;

/**
 * Created on 2025/07/01
 */
@Tool(
        command = "rsa",
        name = "RSA密钥对生成器",
        description = "生成一个RSA密钥对",
        parameters = {"keySize:密钥长度(可选,默认2048)"}
)
public class RsaKeyGeneratorTool {

    private static final int DEFAULT_KEY_SIZE = 2048;

    @ToolMethod
    public static String execute(Map<String, String> parameters) {

        int keySize = DEFAULT_KEY_SIZE;
        if (parameters.containsKey("keySize")) {
            try {
                keySize = Integer.parseInt(parameters.get("keySize"));
            } catch (NumberFormatException e) {
                // 使用默认值
            }
        }

        try {
            // 生成RSA密钥对
            KeyPair keyPair = generateRSAKeyPair(keySize);
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            // 打印公钥和私钥（通常是以字符串形式存储，比如Base64编码）
            return "Public Key: " + Base64.getEncoder().encodeToString(publicKey.getEncoded())+"\n"+"Private Key: " + Base64.getEncoder().encodeToString(privateKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            return "RSA秘钥对生成失败"+e.getMessage();
        }
    }

    private static KeyPair generateRSAKeyPair(Integer keySize) throws NoSuchAlgorithmException {
        // 创建RSA密钥生成器实例
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");

        // 初始化密钥生成器，指定密钥长度
        keyPairGenerator.initialize(keySize);

        // 生成密钥对
        return keyPairGenerator.generateKeyPair();
    }

}
