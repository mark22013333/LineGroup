package com.cheng.linegroup.events;

import com.cheng.linegroup.enums.LineEvent;
import com.cheng.linegroup.events.handler.NoneEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author cheng
 * @since 2024/2/11 14:08
 **/
@Component
public class EventHandlerRegistry {
    private final Map<LineEvent, EventHandler> eventHandlerMap = new EnumMap<>(LineEvent.class);

    @Autowired
    public EventHandlerRegistry(List<EventHandler> handlers) {
        for (EventHandler handler : handlers) {
            eventHandlerMap.put(handler.getSupportedEventType(), handler);
        }
    }

    public EventHandler getEventHandler(LineEvent event) {
        return eventHandlerMap.getOrDefault(event, new NoneEventHandler());
    }
}
