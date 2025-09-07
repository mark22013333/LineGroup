package com.cheng.linegroup.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密碼雜湊產生工具類
 * 用於產生加密後的密碼以便直接插入數據庫
 *
 * @author cheng
 * @since 2025/04/30
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        // 使用與應用相同的加密算法
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 產生 admin123 的雜湊值
        String password = "admin123";
        String encodedPassword = encoder.encode(password);
        
        System.out.println("原始密碼: " + password);
        System.out.println("加密後的密碼: " + encodedPassword);
        
        // 驗證加密是否正確
        boolean matches = encoder.matches(password, encodedPassword);
        System.out.println("驗證結果: " + matches);
    }
}
