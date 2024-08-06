package com.cheng.linegroup.service;

import com.cheng.linegroup.common.contants.RedisPrefix;
import com.cheng.linegroup.dao.ReplyKeywordRepository;
import com.cheng.linegroup.entity.ReplyKeyword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author cheng
 * @since 2024/2/15 23:28
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class ReplyKeywordService {

    private final ReplyKeywordRepository replyKeywordRepository;

    public ReplyKeyword getReplyKeywordByKeywordAndUidAndGid(String keyword, String uid, String gid) {
        if (gid != null) {
            return replyKeywordRepository.getReplyKeywordByKeywordAndGid(keyword, gid);
        } else {
            gid = RedisPrefix.PERSONAL;
        }
        return replyKeywordRepository.getReplyKeywordByKeywordAndUidAndGid(keyword, uid, gid);
    }

    public void save(ReplyKeyword replyKeyword) {
        replyKeywordRepository.save(replyKeyword);
    }

}
