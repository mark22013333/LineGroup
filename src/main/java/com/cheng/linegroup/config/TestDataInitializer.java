package com.cheng.linegroup.config;

import com.cheng.linegroup.dao.SysRoleRepository;
import com.cheng.linegroup.dao.SysUserRepository;
import com.cheng.linegroup.dao.SysUserRoleRepository;
import com.cheng.linegroup.entity.SysRole;
import com.cheng.linegroup.entity.SysUser;
import com.cheng.linegroup.entity.SysUserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 測試數據初始化器
 * 用於在應用啟動時建立測試帳號
 *
 * @author cheng
 * @since 2025/04/30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestDataInitializer implements CommandLineRunner {

    private final SysUserRepository sysUserRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 建立管理員角色
        SysRole adminRole = createAdminRoleIfNotExists();
        
        // 建立測試管理員
        createTestAdmin(adminRole);
        
        log.info("測試數據初始化完成");
    }
    
    private SysRole createAdminRoleIfNotExists() {
        // 查詢所有角色，檢查是否已存在 code 為 ROLE_ADMIN 的角色
        List<SysRole> allRoles = sysRoleRepository.findAll();
        Optional<SysRole> existingRole = allRoles.stream()
                .filter(role -> "ROLE_ADMIN".equals(role.getCode()))
                .findFirst();
        
        if (existingRole.isPresent()) {
            log.info("管理員角色已存在，跳過建立");
            return existingRole.get();
        }
        
        log.info("建立管理員角色");
        SysRole adminRole = new SysRole();
        adminRole.setCode("ROLE_ADMIN");
        adminRole.setName("管理者");
        adminRole.setSort(1);
        adminRole.setStatus(1); // 啟用
        adminRole.setDeleted(0); // 未刪除
        
        return sysRoleRepository.save(adminRole);
    }
    
    private void createTestAdmin(SysRole adminRole) {
        String testUsername = "admin";
        
        // 檢查使用者是否已存在
        Optional<SysUser> existingUser = sysUserRepository.getSysUsersByUsername(testUsername);
        
        if (existingUser.isPresent()) {
            log.info("測試管理員 '{}' 已存在，跳過建立", testUsername);
            return;
        }
        
        log.info("建立測試管理員帳號");
        // 建立測試管理員
        SysUser adminUser = new SysUser();
        adminUser.setUsername(testUsername);
        adminUser.setPassword(passwordEncoder.encode("admin123")); // 設置初始密碼
        adminUser.setNickname("系統管理員");
        adminUser.setStatus(1); // 啟用
        adminUser.setDeleted(0); // 未刪除
        
        SysUser savedUser = sysUserRepository.save(adminUser);
        
        // 分配管理員角色
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(savedUser.getId());
        userRole.setRoleId(adminRole.getId());
        sysUserRoleRepository.save(userRole);
        
        log.info("測試管理員 '{}' 建立成功，密碼: admin123", testUsername);
    }
}
