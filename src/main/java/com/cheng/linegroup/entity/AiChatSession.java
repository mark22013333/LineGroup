package com.cheng.linegroup.entity;

import com.cheng.linegroup.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Cheng
 * @since 2024/8/13 23:56
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AiChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(50)")
    private String userId;

    @Column(columnDefinition = "VARCHAR(50)", unique = true)
    private String sessionId;

    @Column(columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
    private boolean isAiActive;

    @Column(columnDefinition = "VARCHAR(10)")
    private String language;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionStatus sessionStatus = SessionStatus.PENDING; // 預設值

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "aiChatSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AiChatLog> chatLogs;
}
