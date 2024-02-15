package com.cheng.linegroup.service;

import com.cheng.linegroup.dao.LineUserRepository;
import com.cheng.linegroup.entity.LineUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author cheng
 * @since 2024/2/14 01:15
 **/
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
            return null;
        }
    }
}
