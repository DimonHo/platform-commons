package com.platformcommons.notification.api;

import com.platformcommons.notification.api.dto.NotificationResponse;
import com.platformcommons.notification.api.dto.SendNotificationRequest;
import com.platformcommons.notification.api.dto.SendTemplatedNotificationRequest;
import com.platformcommons.notification.api.dto.UnreadCountResponse;
import com.platformcommons.notification.domain.Notification;
import com.platformcommons.notification.domain.NotificationCategory;
import com.platformcommons.notification.domain.NotificationChannel;
import com.platformcommons.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * 消息通知对外接口。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "消息通知", description = "通知发送、查询、已读管理")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 直接发送通知。
     */
    @PostMapping("/api/notifications")
    public NotificationResponse send(@Valid @RequestBody SendNotificationRequest request) {
        log.info("收到发送通知请求：recipientId={}, category={}", request.recipientId(), request.category());
        Notification n = notificationService.send(
                request.recipientId(),
                request.recipientRole(),
                NotificationCategory.valueOf(request.category().toUpperCase()),
                request.title(),
                request.content(),
                request.refType(),
                request.refId(),
                parseChannels(request.channels())
        );
        return toResponse(n);
    }

    /**
     * 基于模板发送通知。
     */
    @PostMapping("/api/notifications/template")
    public NotificationResponse sendByTemplate(@Valid @RequestBody SendTemplatedNotificationRequest request) {
        log.info("收到模板通知请求：templateCode={}, recipientId={}", request.templateCode(), request.recipientId());
        Notification n = notificationService.sendByTemplate(
                request.templateCode(),
                request.recipientId(),
                request.recipientRole(),
                request.refType(),
                request.refId(),
                request.placeholders() != null ? request.placeholders() : Map.of(),
                parseChannels(request.channels())
        );
        return toResponse(n);
    }

    /**
     * 查询用户通知列表。
     */
    @GetMapping("/api/notifications/member/{memberId}")
    public List<NotificationResponse> listByRecipient(@PathVariable Long memberId,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return notificationService.listByRecipient(memberId, page, size).stream()
                .map(NotificationController::toResponse)
                .toList();
    }

    /**
     * 查询未读通知。
     */
    @GetMapping("/api/notifications/member/{memberId}/unread")
    public List<NotificationResponse> listUnread(@PathVariable Long memberId) {
        return notificationService.listUnread(memberId).stream()
                .map(NotificationController::toResponse)
                .toList();
    }

    /**
     * 未读计数。
     */
    @GetMapping("/api/notifications/member/{memberId}/unread-count")
    public UnreadCountResponse countUnread(@PathVariable Long memberId) {
        long count = notificationService.countUnread(memberId);
        return new UnreadCountResponse(memberId, count);
    }

    /**
     * 标记单条已读。
     */
    @PutMapping("/api/notifications/{notificationId}/read")
    public NotificationResponse markAsRead(@PathVariable Long notificationId) {
        Notification n = notificationService.markAsRead(notificationId);
        return toResponse(n);
    }

    /**
     * 批量标记已读。
     */
    @PutMapping("/api/notifications/member/{memberId}/read-all")
    public Map<String, Integer> markAllRead(@PathVariable Long memberId) {
        int count = notificationService.markAllRead(memberId);
        return Map.of("markedRead", count);
    }

    /**
     * 按分类查询。
     */
    @GetMapping("/api/notifications/category/{category}")
    public List<NotificationResponse> listByCategory(@PathVariable String category) {
        NotificationCategory cat = NotificationCategory.valueOf(category.toUpperCase());
        return notificationService.listByCategory(cat).stream()
                .map(NotificationController::toResponse)
                .toList();
    }

    // ===== 内部工具 =====

    private static List<NotificationChannel> parseChannels(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of(NotificationChannel.IN_APP);
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .map(s -> {
                    try {
                        return NotificationChannel.valueOf(s.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return NotificationChannel.IN_APP;
                    }
                })
                .toList();
    }

    private static NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.id(), n.recipientId(), n.recipientRole(), n.category(),
                n.title(), n.content(), n.refType(), n.refId(),
                n.channels(), n.status(), n.readAt(), n.createdAt(), n.sentAt()
        );
    }
}
