package com.cheng.linegroup.services;

import com.cheng.linegroup.dto.system.SystemSettingDTO;

import java.util.List;

/**
 * 系統設置服務介面
 *
 * @author cheng
 * @since 2025/6/8 22:27
 */
public interface SystemSettingService {
    
    /**
     * 取得所有系統設置
     * 
     * @return 系統設置清單
     */
    List<SystemSettingDTO> getAllSettings();
    
    /**
     * 根據類別取得系統設置
     * 
     * @param category 類別
     * @return 系統設置清單
     */
    List<SystemSettingDTO> getSettingsByCategory(String category);
    
    /**
     * 根據 ID 取得系統設置
     * 
     * @param id 系統設置 ID
     * @return 系統設置
     */
    SystemSettingDTO getSettingById(Long id);
    
    /**
     * 根據鍵值取得系統設置
     * 
     * @param key 鍵值
     * @return 系統設置
     */
    SystemSettingDTO getSettingByKey(String key);
    
    /**
     * 取得所有公開的系統設置
     * 
     * @return 公開的系統設置清單
     */
    List<SystemSettingDTO> getPublicSettings();
    
    /**
     * 新增系統設置
     * 
     * @param settingDTO 系統設置資料（包含操作者資訊）
     * @return 新增後的系統設置
     */
    SystemSettingDTO createSetting(SystemSettingDTO settingDTO);
    
    /**
     * 更新系統設置
     * 
     * @param id 系統設置 ID
     * @param settingDTO 系統設置資料（包含操作者資訊）
     * @return 更新後的系統設置
     */
    SystemSettingDTO updateSetting(Long id, SystemSettingDTO settingDTO);
    
    /**
     * 批量更新系統設置
     * 
     * @param settingDTOs 系統設置資料列表（包含操作者資訊）
     * @return 更新後的系統設置列表
     */
    List<SystemSettingDTO> batchUpdateSettings(List<SystemSettingDTO> settingDTOs);
    
    /**
     * 刪除系統設置
     * 
     * @param id 系統設置 ID
     */
    void deleteSetting(Long id);
}
