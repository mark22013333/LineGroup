package com.cheng.linegroup.dao;

import com.cheng.linegroup.entity.ReplyKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author cheng
 * @since 2024/2/15 23:24
 **/
@Repository
public interface ReplyKeywordRepository extends JpaRepository<ReplyKeyword, Long> {

    ReplyKeyword getReplyKeywordByKeywordAndUidAndGid(String keyword, String uid, String gid);

    ReplyKeyword getReplyKeywordByKeywordAndGid(String keyword, String gid);
}
