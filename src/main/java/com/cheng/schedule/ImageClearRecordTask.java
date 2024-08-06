package com.cheng.schedule;

import com.cheng.linegroup.service.ImageSentRecordsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author Cheng
 * @since 2024/8/6 22:59
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageClearRecordTask {

    private final ImageSentRecordsService imageSentRecordsService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void clearImageRecord() {
        log.info("Clear image record task start");
        imageSentRecordsService.deleteAll();
    }
}
