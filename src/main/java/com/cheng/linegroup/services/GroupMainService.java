package com.cheng.linegroup.services;

import com.cheng.linegroup.dao.GroupMainRepository;
import com.cheng.linegroup.entity.GroupMain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

/**
 * @author cheng
 * @since 2024/2/15 00:36
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupMainService {

    private final GroupMainRepository groupMainRepository;

    public GroupMain findByGid(String groupId) {
        try {
            return groupMainRepository.findByGid(groupId);
        } catch (Exception e) {
            log.error("ERR:{}", ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    public void save(GroupMain groupMain) {
        groupMainRepository.save(groupMain);
    }

}
