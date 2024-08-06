package com.cheng.linegroup.service;

import com.cheng.linegroup.dao.ImageSentRecordsRepository;
import com.cheng.linegroup.entity.ImageSentRecords;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Cheng
 * @since 2024/7/29 00:37
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageSentRecordsService {
    private final ImageSentRecordsRepository imageSentRecordsRepository;

    public void saveImageSentRecords(String userId, String groupId, String imageLink) {
        imageSentRecordsRepository.save(ImageSentRecords.builder()
                .uid(userId).groupId(groupId).imageLink(imageLink).build());
    }

    public int countByGroupId(String groupId) {
        return imageSentRecordsRepository.countByGroupId(groupId);
    }

    public int countByGroupIdAndUid(String groupId, String uid) {
        return imageSentRecordsRepository.countByGroupIdAndUid(groupId, uid);
    }
}
