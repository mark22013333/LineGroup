package com.cheng.linegroup.service;

import com.cheng.linegroup.dao.MessageEventRecordRepository;
import com.cheng.linegroup.entity.MessageEventRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Vincent Ta
 * @since 2024/2/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageEventRecordService {

    private final MessageEventRecordRepository messageEventRecordRepository;

    public void save(MessageEventRecord messageEventRecord) {
        messageEventRecordRepository.save(messageEventRecord);
    }
}
