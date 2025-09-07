package com.cheng.linegroup.dao;

import com.cheng.linegroup.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 系統設置資料存取層
 *
 * @author cheng
 * @since 2025/6/8 22:24
 */
@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {
    
    /**
     * 根據鍵值查找設置
     * 
     * @param key 設置的鍵
     * @return 設置值
     */
    Optional<SystemSetting> findByKey(String key);
    
    /**
     * 根據類別查找設置
     * 
     * @param category 設置類別
     * @return 設置列表
     */
    List<SystemSetting> findByCategory(String category);
    
    /**
     * 查詢公開設置
     * 
     * @return 公開設置列表
     */
    List<SystemSetting> findByIsPublicTrue();
    
    /**
     * 根據鍵值更新設置
     * 
     * @param key 設置的鍵
     * @param value 設置的值
     * @param updatedBy 更新者
     * @return 影響的行數
     */
    @Query("UPDATE SystemSetting s SET s.value = :value, s.updatedBy = :updatedBy WHERE s.key = :key")
    int updateValueByKey(String key, String value, String updatedBy);
}
