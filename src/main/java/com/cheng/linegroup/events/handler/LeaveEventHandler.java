package com.cheng.linegroup.events.handler;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.entity.GroupMain;
import com.cheng.linegroup.enums.LineEvent;
import com.cheng.linegroup.events.EventHandler;
import com.cheng.linegroup.services.GroupMainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author cheng
 * @since 2024/2/11 14:05
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveEventHandler implements EventHandler {

    private final GroupMainService groupMainService;

    @Override
    public void handle(WebhookEvent.Event e) {
        log.info("LeaveEventHandler");
        String groupId = e.getSource().getGroupId();
        Optional<GroupMain> getGroup = Optional.ofNullable(groupMainService.findByGid(groupId));

        GroupMain groupMain = getGroup.orElseGet(() -> GroupMain.builder()
                .gid(groupId).isExist(false)
                .build());

        groupMain.setExist(false);
        groupMainService.save(groupMain);
        log.info("退出群組: {}", groupMain);
    }

    @Override
    public LineEvent getSupportedEventType() {
        return LineEvent.LEAVE;
    }
}
