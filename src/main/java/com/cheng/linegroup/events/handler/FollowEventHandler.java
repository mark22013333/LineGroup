package com.cheng.linegroup.events.handler;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.entity.LineUser;
import com.cheng.linegroup.enums.LineEvent;
import com.cheng.linegroup.events.EventHandler;
import com.cheng.linegroup.services.LineService;
import com.cheng.linegroup.services.LineUserService;
import com.cheng.linegroup.services.dto.LineUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author cheng
 * @since 2024/2/11 14:00
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class FollowEventHandler implements EventHandler {

    private final LineUserService lineUserService;
    private final LineService lineService;

    @Override
    public void handle(WebhookEvent.Event e) {
        log.info("FollowEventHandler");
        String userId = e.getSource().getUserId();
        LineUser savedUser;

        try {
            savedUser = saveOrUpdateUser(userId);
            log.info("User saved: {}", savedUser);
        } catch (Exception ex) {
            log.error("===> ERR handling FollowEvent for user ID {}: {}", userId, ex.getMessage(), ex);
        }
    }

    @Override
    public LineEvent getSupportedEventType() {
        return LineEvent.FOLLOW;
    }

    private LineUser saveOrUpdateUser(String userId) throws IOException {
        LineUser existingUser = lineUserService.findByUid(userId);
        LineUserDto userProfile = lineService.getUserProfile(userId);

        if (existingUser != null) {
            existingUser.setFriend(true)
                    .setAvatar(userProfile.getPictureUrl())
                    .setNickname(userProfile.getDisplayName())
                    .setStatusMessage(userProfile.getStatusMessage());

            return lineUserService.save(existingUser);
        } else {
            LineUser newUser = LineUser.builder()
                    .uid(userId)
                    .avatar(userProfile.getPictureUrl())
                    .nickname(userProfile.getDisplayName())
                    .statusMessage(userProfile.getStatusMessage())
                    .isFriend(true)
                    .build();
            return lineUserService.save(newUser);
        }
    }
}
