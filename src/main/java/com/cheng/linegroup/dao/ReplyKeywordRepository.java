package com.cheng.linegroup.dao;

import com.cheng.linegroup.entity.ReplyKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author cheng
 * @since 2024/2/15 23:24
 **/
@Repository
public interface ReplyKeywordRepository extends JpaRepository<ReplyKeyword, Integer> {

    ReplyKeyword getReplyKeywordByKeyword(String keyword);

}
