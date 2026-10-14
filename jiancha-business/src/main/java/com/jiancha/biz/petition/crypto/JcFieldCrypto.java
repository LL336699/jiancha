package com.jiancha.biz.petition.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.StringUtils;

/**
 * 字段级加密服务（AES-256-GCM）
 *
 * <p>用于举报人敏感信息（姓名 / 电话 / 身份证）的加密存储：</p>
 * <ul>
 *   <li>密钥由配置 {@code jiancha.petition.crypto-key} 经 SHA-256 派生为 256 位，支持环境变量注入，不硬编码；</li>
 *   <li>每次加密生成 12 字节随机 IV，GCM 128 位认证标签，密文格式 {@code Base64(IV || 密文+Tag)}；</li>
 *   <li>解密兼容历史明文（非 Base64/GCM 结构时原样返回），避免升级期数据不可读；</li>
 *   <li>脱敏展示单独提供 mask 方法，管理端列表不落明文。</li>
 * </ul>
 *
 * @author 小标
 */
@Component
public class JcFieldCrypto {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private static final int IV_LENGTH = 12;

    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKey secretKey;

    public JcFieldCrypto(@Value("${jiancha.petition.crypto-key:jiancha-default-crypto-key-change-me}") String cryptoKey) {
        // 配置串经 SHA-256 派生为 256 位 AES 密钥；生产环境必须用环境变量注入强随机值
        this.secretKey = new SecretKeySpec(sha256(cryptoKey), "AES");
    }

    /**
     * 加密明文，返回 Base64(IV || 密文+Tag)。明文为空时原样返回 null/空串。
     */
    public String encrypt(String plain) {
        if (StringUtils.isEmpty(plain)) {
            return plain;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[IV_LENGTH + cipherText.length];
            System.arraycopy(iv, 0, out, 0, IV_LENGTH);
            System.arraycopy(cipherText, 0, out, IV_LENGTH, cipherText.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("字段加密失败", e);
        }
    }

    /**
     * 解密 Base64(IV || 密文+Tag)。输入不是合法密文结构时按历史明文原样返回。
     */
    public String decrypt(String stored) {
        if (StringUtils.isEmpty(stored)) {
            return stored;
        }
        try {
            byte[] raw = Base64.getDecoder().decode(stored);
            if (raw.length <= IV_LENGTH) {
                return stored;
            }
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(raw, 0, iv, 0, IV_LENGTH);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] plain = cipher.doFinal(raw, IV_LENGTH, raw.length - IV_LENGTH);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 密钥不匹配或数据被篡改时 GCM 校验失败，按明文兜底并交由上层展示
            return stored;
        }
    }

    /**
     * 脱敏展示：保留首尾各 1 位，中间以 * 代替；长度 ≤2 时全掩码。
     */
    public static String mask(String plain) {
        if (StringUtils.isEmpty(plain)) {
            return plain;
        }
        int len = plain.length();
        if (len <= 2) {
            return "*".repeat(len);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(plain.charAt(0));
        sb.append("*".repeat(len - 2));
        sb.append(plain.charAt(len - 1));
        return sb.toString();
    }

    /**
     * 生成一把随机 AES-256 密钥的 Base64 串（供部署时生成 crypto-key 使用）。
     */
    public static String generateKey() {
        try {
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            gen.init(256);
            return Base64.getEncoder().encodeToString(gen.generateKey().getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
