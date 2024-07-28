package com.cheng.linegroup.events.handler;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.entity.LineUser;
import com.cheng.linegroup.enums.LineEvent;
import com.cheng.linegroup.events.EventHandler;
import com.cheng.linegroup.service.LineUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author cheng
 * @since 2024/2/11 14:03
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class UnFollowEventHandler implements EventHandler {

    private final LineUserService lineUserService;

    @Override
    public void handle(WebhookEvent.Event e) {
        log.info("UnFollowEventHandler");
        String userId = e.getSource().getUserId();
        Optional<LineUser> existingUser = Optional.ofNullable(lineUserService.findByUid(userId));

        LineUser user = existingUser.orElseGet(() -> LineUser.builder().uid(userId).build());
        user.setFriend(false);
        log.info("User unfollow:{}", user);

        LineUser savedUser = lineUserService.save(user);
        log.info("User save:{}", savedUser);
    }

    @Override
    public LineEvent getSupportedEventType() {
        return LineEvent.UNFOLLOW;
    }
}
