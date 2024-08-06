package com.cheng.linegroup.dao;

import com.cheng.linegroup.entity.ImageSentRecords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Cheng
 * @since 2024/7/29 00:36
 **/
@Repository
public interface ImageSentRecordsRepository extends JpaRepository<ImageSentRecords, Long> {

    int countByGroupId(String groupId);

    int countByGroupIdAndUid(String groupId, String uid);
}
