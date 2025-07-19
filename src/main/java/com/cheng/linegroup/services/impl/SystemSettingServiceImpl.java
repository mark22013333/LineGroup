package com.cheng.linegroup.services.impl;

import com.cheng.linegroup.dao.SystemSettingRepository;
import com.cheng.linegroup.dto.system.SystemSettingDTO;
import com.cheng.linegroup.entity.SystemSetting;
import com.cheng.linegroup.exception.ResourceNotFoundException;
import com.cheng.linegroup.services.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系統設置服務實作
 *
 * @author cheng
 * @since 2025/6/8 22:28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;

    @Override
    public List<SystemSettingDTO> getAllSettings() {
        log.info("取得所有系統設置");
        return systemSettingRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SystemSettingDTO> getSettingsByCategory(String category) {
        log.info("依照類別取得系統設置: {}", category);
        return systemSettingRepository.findByCategory(category).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public SystemSettingDTO getSettingById(Long id) {
        log.info("依照 ID 取得系統設置: {}", id);
        return systemSettingRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("找不到指定的系統設置: " + id));
    }

    @Override
    public SystemSettingDTO getSettingByKey(String key) {
        log.info("依照鍵值取得系統設置: {}", key);
        return systemSettingRepository.findByKey(key)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("找不到指定的系統設置鍵值: " + key));
    }
    
    @Override
    public List<SystemSettingDTO> getPublicSettings() {
        log.info("取得所有公開的系統設置");
        return systemSettingRepository.findByIsPublicTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SystemSettingDTO createSetting(SystemSettingDTO settingDTO) {
        log.info("建立新的系統設置: {}", settingDTO.getKey());
        SystemSetting setting = convertToEntity(settingDTO);
        setting.setCreatedBy(settingDTO.getCreatedBy());
        setting.setUpdatedBy(settingDTO.getUpdatedBy());
        SystemSetting savedSetting = systemSettingRepository.save(setting);
        return convertToDto(savedSetting);
    }

    @Override
    @Transactional
    public SystemSettingDTO updateSetting(Long id, SystemSettingDTO settingDTO) {
        log.info("更新系統設置: {}", id);
        SystemSetting existingSetting = systemSettingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到要更新的系統設置: " + id));
        
        BeanUtils.copyProperties(settingDTO, existingSetting, "id", "createdAt", "createdBy");
        existingSetting.setUpdatedBy(settingDTO.getUpdatedBy());
        SystemSetting updatedSetting = systemSettingRepository.save(existingSetting);
        return convertToDto(updatedSetting);
    }

    @Override
    @Transactional
    public List<SystemSettingDTO> batchUpdateSettings(List<SystemSettingDTO> settingDTOs) {
        log.info("批量更新 {} 個系統設置", settingDTOs.size());
        List<SystemSetting> settings = settingDTOs.stream()
                .map(dto -> {
                    SystemSetting setting;
                    if (dto.getId() != null) {
                        setting = systemSettingRepository.findById(dto.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("找不到要更新的系統設置: " + dto.getId()));
                        BeanUtils.copyProperties(dto, setting, "createdAt", "createdBy");
                    } else {
                        setting = convertToEntity(dto);
                        setting.setCreatedBy(dto.getCreatedBy());
                    }
                    setting.setUpdatedBy(dto.getUpdatedBy());
                    return setting;
                })
                .collect(Collectors.toList());
        
        List<SystemSetting> updatedSettings = systemSettingRepository.saveAll(settings);
        return updatedSettings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSetting(Long id) {
        log.info("刪除系統設置: {}", id);
        if (!systemSettingRepository.existsById(id)) {
            throw new ResourceNotFoundException("找不到要刪除的系統設置: " + id);
        }
        systemSettingRepository.deleteById(id);
    }

    /**
     * 將實體轉換為 DTO
     */
    private SystemSettingDTO convertToDto(SystemSetting setting) {
        SystemSettingDTO dto = new SystemSettingDTO();
        BeanUtils.copyProperties(setting, dto);
        return dto;
    }

    /**
     * 將 DTO 轉換為實體
     */
    private SystemSetting convertToEntity(SystemSettingDTO dto) {
        SystemSetting setting = new SystemSetting();
        BeanUtils.copyProperties(dto, setting);
        return setting;
    }
}
