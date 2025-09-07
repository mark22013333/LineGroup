package com.cheng.linegroup.services;

import com.cheng.linegroup.dto.user.UserDTO;
import com.cheng.linegroup.dto.user.UserQueryParams;
import com.cheng.linegroup.services.common.BaseQueryService;

/**
 * 使用者管理服務介面
 * 
 * @author cheng
 * @since 2025/5/3 12:00
 */
public interface UserManagementService extends BaseQueryService<UserDTO, UserQueryParams> {
    
    /**
     * 根據ID查詢使用者
     * 
     * @param id 使用者ID
     * @return 使用者資訊
     */
    UserDTO findById(Long id);
    
    /**
     * 更新使用者最後登入時間
     * 
     * @param username 使用者名稱
     * @return 是否更新成功
     */
    boolean updateLastLoginTime(String username);
}
