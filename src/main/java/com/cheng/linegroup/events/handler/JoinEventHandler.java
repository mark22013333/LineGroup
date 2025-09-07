package com.cheng.linegroup.events.handler;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.entity.GroupMain;
import com.cheng.linegroup.enums.LineEvent;
import com.cheng.linegroup.events.EventHandler;
import com.cheng.linegroup.services.GroupMainService;
import com.cheng.linegroup.services.LineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author cheng
 * @since 2024/2/11 14:04
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class JoinEventHandler implements EventHandler {

    private final LineService lineService;
    private final GroupMainService groupMainService;

    @Override
    public void handle(WebhookEvent.Event e) {
        log.info("JoinEventHandler");
        String groupId = e.getSource().getGroupId();
        Optional<GroupMain> getGroup = Optional.ofNullable(groupMainService.findByGid(groupId));

        GroupMain groupMain = handleGroup(groupId, getGroup);
        groupMainService.save(groupMain);
        log.info("加入群組: {}", groupMain);
    }

    @Override
    public LineEvent getSupportedEventType() {
        return LineEvent.JOIN;
    }

    /**
     * 將資料庫取出的{@link GroupMain}進行處理
     *
     * @param groupId  群組ID
     * @param getGroup 從資料庫取出的{@link Optional<GroupMain>}
     * @return {@link GroupMain}
     */
    private GroupMain handleGroup(String groupId, Optional<GroupMain> getGroup) {
        String groupName;
        try {
            groupName = lineService.getGroupName(groupId);
        } catch (Exception ex) {
            log.error("ERR:{}", ex.getMessage());
            groupName = "Unknown";
        }

        String finalGroupName = groupName;
        GroupMain groupMain = getGroup.orElseGet(() -> {
            log.info("JoinEventHandler - groupId: {}, groupName: {}", groupId, finalGroupName);
            return GroupMain.builder().name(finalGroupName).gid(groupId).build();
        });

        if (StringUtils.isBlank(groupMain.getName())) {
            groupMain.setName(groupName);
        }

        groupMain.setExist(true);
        return groupMain;
    }
}
