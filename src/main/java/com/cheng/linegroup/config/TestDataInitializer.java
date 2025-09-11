package com.cheng.linegroup.config;

import com.cheng.linegroup.dao.SysRoleRepository;
import com.cheng.linegroup.dao.SysUserRepository;
import com.cheng.linegroup.dao.SysUserRoleRepository;
import com.cheng.linegroup.dao.SystemSettingRepository;
import com.cheng.linegroup.entity.SysRole;
import com.cheng.linegroup.entity.SysUser;
import com.cheng.linegroup.entity.SysUserRole;
import com.cheng.linegroup.entity.SystemSetting;
import com.cheng.linegroup.entity.SysPermission;
import com.cheng.linegroup.entity.SysRolePermission;
import com.cheng.linegroup.repository.SysPermissionRepository;
import com.cheng.linegroup.repository.SysRolePermissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 測試數據初始化器
 * 用於在應用啟動時建立測試帳號和基礎設定
 *
 * @author cheng
 * @since 2025/04/30
 */
@Slf4j
@Component
public class TestDataInitializer implements CommandLineRunner {

    private final SysUserRepository sysUserRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    @Qualifier("daoSysRoleRepository")
    private final SysRoleRepository sysRoleRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final SysPermissionRepository permissionRepository;
    private final SysRolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataInitializer(
            SysUserRepository sysUserRepository,
            SysUserRoleRepository sysUserRoleRepository,
            @Qualifier("daoSysRoleRepository")
            SysRoleRepository sysRoleRepository,
            SystemSettingRepository systemSettingRepository,
            SysPermissionRepository permissionRepository,
            SysRolePermissionRepository rolePermissionRepository,
            PasswordEncoder passwordEncoder) {
        this.sysUserRepository = sysUserRepository;
        this.sysUserRoleRepository = sysUserRoleRepository;
        this.sysRoleRepository = sysRoleRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("開始執行測試數據初始化...");
        
        try {
            // 建立角色
            SysRole adminRole = createAdminRoleIfNotExists();
            SysRole operatorRole = createOperatorRoleIfNotExists();
            
            // 建立測試管理員
            createTestAdmin(adminRole);
            
            // 建立測試操作員
            createTestOperator(operatorRole);
            
            // 建立系統設置
            initializeSystemSettings();
            
            // 建立權限
            initializePermissions(adminRole, operatorRole);
            
            log.info("測試數據初始化完成");
        } catch (Exception e) {
            log.error("測試數據初始化失敗", e);
        }
    }
    
    private SysRole createAdminRoleIfNotExists() {
        // 查詢角色，檢查是否已存在 code 為 ROLE_ADMIN 的角色
        Optional<SysRole> existingRole = sysRoleRepository.findByCode("ROLE_ADMIN");
                
        // 如果已存在，直接返回
        if (existingRole.isPresent()) {
            log.info("管理員角色已存在，無需再次建立");
            return existingRole.get();
        }
        
        // 否則建立新角色
        SysRole adminRole = SysRole.builder()
                .name("系統管理員")
                .code("ROLE_ADMIN")
                .status(1)  // 啟用狀態
                .sort(1)    // 排序值
                .description("系統最高權限角色，擁有所有功能的管理權限")
                .deleted(0) // 未刪除
                .dataScope(0) // 所有資料
                .build();
        
        return sysRoleRepository.save(adminRole);
    }
    
    private SysRole createOperatorRoleIfNotExists() {
        // 查詢角色，檢查是否已存在 code 為 ROLE_OPERATOR 的角色
        Optional<SysRole> existingRole = sysRoleRepository.findByCode("ROLE_OPERATOR");
                
        // 如果已存在，直接返回
        if (existingRole.isPresent()) {
            log.info("操作員角色已存在，無需再次建立");
            return existingRole.get();
        }
        
        // 否則建立新角色
        SysRole operatorRole = SysRole.builder()
                .name("一般操作員")
                .code("ROLE_OPERATOR")
                .status(1)  // 啟用狀態
                .sort(2)    // 排序值
                .description("一般操作權限，可執行日常工作但無管理功能")
                .deleted(0) // 未刪除
                .dataScope(3) // 本人資料
                .build();
        
        return sysRoleRepository.save(operatorRole);
    }
    
    private void createTestAdmin(SysRole adminRole) {
        // 檢查是否已存在用戶名為 'admin' 的用戶
        Optional<SysUser> existingAdmin = sysUserRepository.getSysUsersByUsername("admin");
        
        if (existingAdmin.isPresent()) {
            log.info("管理員帳號已存在，無需再次建立");
            return;
        }
        
        // 建立新的管理員帳號
        SysUser admin = SysUser.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .nickname("系統管理員")
                .email("admin@example.com")
                .phone("13800138000")
                .gender(1)
                .status(1)  // 啟用狀態
                .deleted(0) // 未刪除
                .build();
        
        SysUser savedAdmin = sysUserRepository.save(admin);
        
        // 建立用戶-角色關聯
        SysUserRole userRole = SysUserRole.builder()
                .userId(savedAdmin.getId())
                .roleId(adminRole.getId())
                .build();
        
        sysUserRoleRepository.save(userRole);
        
        log.info("已建立管理員帳號: {} ({})", admin.getUsername(), admin.getNickname());
    }
    
    private void createTestOperator(SysRole operatorRole) {
        // 檢查是否已存在用戶名為 'operator' 的用戶
        Optional<SysUser> existingUser = sysUserRepository.getSysUsersByUsername("operator");
        
        if (existingUser.isPresent()) {
            log.info("操作員帳號已存在，無需再次建立");
            return;
        }
        
        // 建立新的操作員帳號
        SysUser operator = SysUser.builder()
                .username("operator")
                .password(passwordEncoder.encode("operator"))
                .nickname("一般操作員")
                .email("operator@example.com")
                .phone("13900139000")
                .gender(1)
                .status(1)  // 啟用狀態
                .deleted(0) // 未刪除
                .build();
        
        SysUser savedOperator = sysUserRepository.save(operator);
        
        // 建立用戶-角色關聯
        SysUserRole userRole = SysUserRole.builder()
                .userId(savedOperator.getId())
                .roleId(operatorRole.getId())
                .build();
        
        sysUserRoleRepository.save(userRole);
        
        log.info("已建立操作員帳號: {} ({})", operator.getUsername(), operator.getNickname());
    }
    
    private void initializeSystemSettings() {
        log.info("開始初始化系統設置...");
        
        // 定義要建立的系統設置列表
        List<SystemSetting> settings = Arrays.asList(
            // 網站基本設置
            SystemSetting.builder()
                .key("site.name")
                .value("LineGroup 管理系統")
                .description("網站名稱")
                .category("基本設置")
                .isPublic(true)
                .createdBy("system")
                .updatedBy("system")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),
                
            SystemSetting.builder()
                .key("site.description")
                .value("LINE 多群組管理工具")
                .description("網站描述")
                .category("基本設置")
                .isPublic(true)
                .createdBy("system")
                .updatedBy("system")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),
                
            SystemSetting.builder()
                .key("site.logo")
                .value("/img/logo.png")
                .description("網站 Logo 路徑")
                .category("基本設置")
                .isPublic(true)
                .createdBy("system")
                .updatedBy("system")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),
                
            // 安全設置
            SystemSetting.builder()
                .key("security.token.expiration")
                .value("86400")
                .description("登入令牌有效期限（秒）")
                .category("安全設置")
                .isPublic(false)
                .createdBy("system")
                .updatedBy("system")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),
                
            SystemSetting.builder()
                .key("security.password.minLength")
                .value("6")
                .description("密碼最小長度")
                .category("安全設置")
                .isPublic(false)
                .createdBy("system")
                .updatedBy("system")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),
                
            SystemSetting.builder()
                .key("security.login.maxRetries")
                .value("5")
                .description("最大登入重試次數")
                .category("安全設置")
                .isPublic(false)
                .createdBy("system")
                .updatedBy("system")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),
                
            // LINE Bot 設置
            SystemSetting.builder()
                .key("line.bot.channelSecret")
                .value("")
                .description("LINE Bot Channel Secret")
                .category("LINE 設置")
                .isPublic(false)
                .createdBy("system")
                .updatedBy("system")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build(),
                
            SystemSetting.builder()
                .key("line.bot.channelToken")
                .value("")
                .description("LINE Bot Channel Access Token")
                .category("LINE 設置")
                .isPublic(false)
                .createdBy("system")
                .updatedBy("system")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );
        
        // 檢查每個設置是否已存在，如果不存在則建立
        for (SystemSetting setting : settings) {
            Optional<SystemSetting> existingSetting = systemSettingRepository.findByKey(setting.getKey());
            
            if (existingSetting.isPresent()) {
                log.debug("系統設置 [{}] 已存在，跳過建立", setting.getKey());
            } else {
                systemSettingRepository.save(setting);
                log.debug("已建立系統設置: {}", setting.getKey());
            }
        }
        
        log.info("系統設置初始化完成，共處理 {} 個設置項目", settings.size());
    }
    
    /**
     * 初始化權限並分配給角色
     */
    private void initializePermissions(SysRole adminRole, SysRole operatorRole) {
        log.info("開始初始化權限...");
        
        // 定義各模組的權限
        Map<String, List<SysPermission>> modulePermissions = new HashMap<>();
        
        // 系統管理模組
        List<SysPermission> systemPermissions = new ArrayList<>();
        
        // 系統設置權限
        systemPermissions.add(createPermissionIfNotExists("系統管理", "系統設置", "system:settings:view", "查看系統設置", 10));
        systemPermissions.add(createPermissionIfNotExists("系統管理", "系統設置", "system:settings:edit", "編輯系統設置", 11));
        
        // 使用者管理權限
        systemPermissions.add(createPermissionIfNotExists("系統管理", "使用者管理", "system:users:view", "查看使用者列表", 20));
        systemPermissions.add(createPermissionIfNotExists("系統管理", "使用者管理", "system:users:add", "新增使用者", 21));
        systemPermissions.add(createPermissionIfNotExists("系統管理", "使用者管理", "system:users:edit", "編輯使用者", 22));
        systemPermissions.add(createPermissionIfNotExists("系統管理", "使用者管理", "system:users:delete", "刪除使用者", 23));
        
        // 角色權限管理
        systemPermissions.add(createPermissionIfNotExists("系統管理", "角色權限", "system:roles:view", "查看角色列表", 30));
        systemPermissions.add(createPermissionIfNotExists("系統管理", "角色權限", "system:roles:add", "新增角色", 31));
        systemPermissions.add(createPermissionIfNotExists("系統管理", "角色權限", "system:roles:edit", "編輯角色", 32));
        systemPermissions.add(createPermissionIfNotExists("系統管理", "角色權限", "system:roles:delete", "刪除角色", 33));
        systemPermissions.add(createPermissionIfNotExists("系統管理", "角色權限", "system:roles:assign", "分配權限", 34));
        
        modulePermissions.put("系統管理", systemPermissions);
        
        // LINE Bot 管理模組
        List<SysPermission> lineBotPermissions = new ArrayList<>();
        
        lineBotPermissions.add(createPermissionIfNotExists("LINE Bot", "群組管理", "line:groups:view", "查看群組列表", 10));
        lineBotPermissions.add(createPermissionIfNotExists("LINE Bot", "群組管理", "line:groups:edit", "編輯群組資訊", 11));
        lineBotPermissions.add(createPermissionIfNotExists("LINE Bot", "消息管理", "line:messages:view", "查看消息歷史", 20));
        lineBotPermissions.add(createPermissionIfNotExists("LINE Bot", "消息管理", "line:messages:send", "發送消息", 21));
        
        modulePermissions.put("LINE Bot", lineBotPermissions);
        
        // 庫存管理模組
        List<SysPermission> inventoryPermissions = new ArrayList<>();
        
        // 物品管理權限
        inventoryPermissions.add(createPermissionIfNotExists("庫存管理", "物品管理", "inventory:items:view", "查看物品列表", 10));
        inventoryPermissions.add(createPermissionIfNotExists("庫存管理", "物品管理", "inventory:items:add", "新增物品", 11));
        inventoryPermissions.add(createPermissionIfNotExists("庫存管理", "物品管理", "inventory:items:edit", "編輯物品", 12));
        inventoryPermissions.add(createPermissionIfNotExists("庫存管理", "物品管理", "inventory:items:delete", "刪除物品", 13));
        
        // 庫存管理權限
        inventoryPermissions.add(createPermissionIfNotExists("庫存管理", "庫存管理", "inventory:stock:view", "查看庫存", 20));
        inventoryPermissions.add(createPermissionIfNotExists("庫存管理", "庫存管理", "inventory:stock:adjust", "調整庫存", 21));
        
        // 條碼管理權限
        inventoryPermissions.add(createPermissionIfNotExists("庫存管理", "條碼管理", "inventory:barcode:scan", "掃描條碼", 30));
        inventoryPermissions.add(createPermissionIfNotExists("庫存管理", "條碼管理", "inventory:barcode:generate", "產生條碼", 31));
        
        // 借還管理權限
        inventoryPermissions.add(createPermissionIfNotExists("庫存管理", "借還管理", "inventory:borrow:view", "查看借還記錄", 40));
        inventoryPermissions.add(createPermissionIfNotExists("庫存管理", "借還管理", "inventory:borrow:create", "建立借用", 41));
        inventoryPermissions.add(createPermissionIfNotExists("庫存管理", "借還管理", "inventory:borrow:return", "歸還物品", 42));
        
        // 報表權限
        inventoryPermissions.add(createPermissionIfNotExists("庫存管理", "報表管理", "inventory:reports:view", "查看報表", 50));
        inventoryPermissions.add(createPermissionIfNotExists("庫存管理", "報表管理", "inventory:reports:export", "匯出報表", 51));
        
        modulePermissions.put("庫存管理", inventoryPermissions);
        
        // 分配權限給角色
        // 管理員角色擁有所有權限
        List<Long> adminPermissionIds = new ArrayList<>();
        for (List<SysPermission> permissions : modulePermissions.values()) {
            for (SysPermission permission : permissions) {
                adminPermissionIds.add(permission.getId());
            }
        }
        assignPermissionsToRole(adminRole.getId(), adminPermissionIds);
        
        // 操作員角色擁有查看權限、LINE Bot 操作權限和庫存管理權限
        List<Long> operatorPermissionIds = new ArrayList<>();
        // 系統管理中的查看權限
        for (SysPermission permission : modulePermissions.get("系統管理")) {
            if (permission.getActionKey().endsWith(":view")) {
                operatorPermissionIds.add(permission.getId());
            }
        }
        // LINE Bot 的所有權限
        for (SysPermission permission : modulePermissions.get("LINE Bot")) {
            operatorPermissionIds.add(permission.getId());
        }
        // 庫存管理的所有權限
        for (SysPermission permission : modulePermissions.get("庫存管理")) {
            operatorPermissionIds.add(permission.getId());
        }
        assignPermissionsToRole(operatorRole.getId(), operatorPermissionIds);
        
        log.info("權限初始化完成，共建立 {} 個模組的權限", modulePermissions.size());
    }
    
    /**
     * 建立權限（如果不存在）
     */
    private SysPermission createPermissionIfNotExists(String module, String feature, String actionKey, String description, Integer sort) {
        // 檢查權限是否已存在
        Optional<SysPermission> existingPermission = permissionRepository.findByActionKey(actionKey);
        
        if (existingPermission.isPresent()) {
            log.debug("權限 [{}] 已存在，跳過建立", actionKey);
            return existingPermission.get();
        }
        
        // 建立新權限
        SysPermission permission = SysPermission.builder()
                .module(module)
                .feature(feature)
                .actionKey(actionKey)
                .description(description)
                .sort(sort)
                .build();
        
        permission = permissionRepository.save(permission);
        log.debug("已建立權限: {}", actionKey);
        
        return permission;
    }
    
    /**
     * 為角色指派權限
     */
    private void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        // 先清除該角色原有的權限
        rolePermissionRepository.deleteByRoleId(roleId);
        
        // 為角色分配新權限
        for (Long permissionId : permissionIds) {
            SysRolePermission rolePermission = SysRolePermission.builder()
                    .roleId(roleId)
                    .permissionId(permissionId)
                    .build();
                    
            rolePermissionRepository.save(rolePermission);
        }
        
        log.debug("已為角色 ID [{}] 分配 {} 個權限", roleId, permissionIds.size());
    }
}
