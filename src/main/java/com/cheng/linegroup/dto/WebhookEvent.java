package com.cheng.linegroup.dto;

import lombok.Data;

import java.util.List;

/**
 * @author cheng
 * @since 2023/12/3 1:13 AM
 **/
@Data
public class WebhookEvent {
    private String destination;
    private List<Event> events;

    @Data
    public static class Event {
        private String type;
        private String webhookEventId;
        private DeliveryContext deliveryContext;
        private Message message;
        private long timestamp;
        private Source source;
        private String replyToken;
        private String mode;

        @Data
        public static class DeliveryContext {
            private boolean isRedelivery;
        }

        @Data
        public static class Source {
            private String type;
            private String groupId;
            private String userId;
        }

        @Data
        public static class Message {
            private String type;
            private String id;
            private String text;
            private String quoteToken;
            private String stickerId;
            private String packageId;
            private String stickerResourceType;
            private List<String> keywords;
        }
    }
}
