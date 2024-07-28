package com.cheng.linegroup.service;

import com.cheng.linegroup.dao.LineUserRepository;
import com.cheng.linegroup.entity.LineUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

/**
 * @author cheng
 * @since 2024/2/14 01:15
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class LineUserService {

    private final LineUserRepository lineUserRepository;

    public LineUser save(LineUser lineUser) {
        return lineUserRepository.save(lineUser);
    }

    public LineUser findByUid(String uid) {
        try {
            return lineUserRepository.findByUid(uid);
        } catch (Exception e) {
            log.error("ERR:{}", ExceptionUtils.getStackTrace(e));
            return null;
        }
    }
}
