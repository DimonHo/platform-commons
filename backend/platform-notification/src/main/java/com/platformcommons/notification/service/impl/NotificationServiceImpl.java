package com.platformcommons.notification.service.impl;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.notification.domain.Notification;
import com.platformcommons.notification.domain.NotificationCategory;
import com.platformcommons.notification.domain.NotificationChannel;
import com.platformcommons.notification.domain.NotificationStatus;
import com.platformcommons.notification.domain.NotificationTemplate;
import com.platformcommons.notification.repository.NotificationRepository;
import com.platformcommons.notification.repository.NotificationTemplateRepository;
import com.platformcommons.notification.repository.entity.NotificationEntity;
import com.platformcommons.notification.repository.entity.NotificationTemplateEntity;
import com.platformcommons.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * 通知服务实现。
 *
 * <p>负责通知创建、投递模拟、已读标记与查询。
 * 站内通知即时标记为 SENT，SMS/PUSH 模拟投递后标记 SENT。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Notification send(Long recipientId, String recipientRole, NotificationCategory category,
                             String title, String content, String refType, String refId,
                             List<NotificationChannel> channels) {
        log.info("发送通知：recipientId={}, category={}, title={}", recipientId, category, title);

        NotificationEntity entity = new NotificationEntity();
        entity.setRecipientId(recipientId);
        entity.setRecipientRole(recipientRole);
        entity.setCategory(category);
        entity.setTitle(title);
        entity.setContent(content);
        entity.setRefType(refType);
        entity.setRefId(refId);
        entity.setChannels(serializeChannels(channels));
        entity.setStatus(NotificationStatus.SENT);
        entity.setSentAt(Instant.now());
        entity.setCreatedAt(Instant.now());

        NotificationEntity saved = notificationRepository.save(entity);
        log.info("通知发送成功：id={}, recipientId={}", saved.getId(), recipientId);
        return toDomain(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Notification sendByTemplate(String templateCode, Long recipientId, String recipientRole,
                                       String refType, String refId, Map<String, String> placeholders,
                                       List<NotificationChannel> channels) {
        log.info("基于模板发送通知：templateCode={}, recipientId={}", templateCode, recipientId);

        NotificationTemplateEntity template = templateRepository.findByCodeAndEnabledTrue(templateCode)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND,
                        "通知模板不存在或已禁用: " + templateCode));

        String title = renderTemplate(template.getTitleTemplate(), placeholders);
        String content = renderTemplate(template.getContentTemplate(), placeholders);

        List<NotificationChannel> effectiveChannels = (channels != null && !channels.isEmpty())
                ? channels
                : parseChannels(template.getDefaultChannels());

        return send(recipientId, recipientRole, template.getCategory(),
                title, content, refType, refId, effectiveChannels);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> listByRecipient(Long recipientId, int page, int size) {
        Page<NotificationEntity> result = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(
                recipientId, PageRequest.of(page, Math.min(size, 100)));
        return result.stream().map(NotificationServiceImpl::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> listUnread(Long recipientId) {
        return notificationRepository
                .findByRecipientIdAndStatusOrderByCreatedAtDesc(recipientId, NotificationStatus.SENT)
                .stream()
                .map(NotificationServiceImpl::toDomain)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Notification markAsRead(Long notificationId) {
        log.info("标记通知已读：id={}", notificationId);
        NotificationEntity entity = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND,
                        "通知不存在: " + notificationId));

        if (NotificationStatus.READ.equals(entity.getStatus())) {
            return toDomain(entity);
        }

        entity.setStatus(NotificationStatus.READ);
        entity.setReadAt(Instant.now());
        NotificationEntity saved = notificationRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markAllRead(Long recipientId) {
        log.info("批量标记已读：recipientId={}", recipientId);
        List<NotificationEntity> unread = notificationRepository
                .findByRecipientIdAndStatusOrderByCreatedAtDesc(recipientId, NotificationStatus.SENT);
        Instant now = Instant.now();
        unread.forEach(e -> {
            e.setStatus(NotificationStatus.READ);
            e.setReadAt(now);
        });
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long recipientId) {
        return notificationRepository.countByRecipientIdAndStatus(recipientId, NotificationStatus.SENT);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> listByCategory(NotificationCategory category) {
        return notificationRepository.findByCategoryAndStatus(category, NotificationStatus.SENT)
                .stream()
                .map(NotificationServiceImpl::toDomain)
                .toList();
    }

    // ===== 内部工具 =====

    /**
     * 渲染模板占位符：将 {key} 替换为实际值。
     */
    private String renderTemplate(String template, Map<String, String> placeholders) {
        if (template == null) {
            return "";
        }
        if (placeholders == null || placeholders.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    private static String serializeChannels(List<NotificationChannel> channels) {
        if (channels == null || channels.isEmpty()) {
            return NotificationChannel.IN_APP.name();
        }
        return channels.stream()
                .map(NotificationChannel::name)
                .reduce((a, b) -> a + "," + b)
                .orElse(NotificationChannel.IN_APP.name());
    }

    private static List<NotificationChannel> parseChannels(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of(NotificationChannel.IN_APP);
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .map(s -> {
                    try {
                        return NotificationChannel.valueOf(s);
                    } catch (IllegalArgumentException e) {
                        return NotificationChannel.IN_APP;
                    }
                })
                .toList();
    }

    private static List<NotificationChannel> deserializeChannels(String raw) {
        return parseChannels(raw);
    }

    private static Notification toDomain(NotificationEntity entity) {
        return new Notification(
                entity.getId(),
                entity.getRecipientId(),
                entity.getRecipientRole(),
                entity.getCategory(),
                entity.getTitle(),
                entity.getContent(),
                entity.getRefType(),
                entity.getRefId(),
                deserializeChannels(entity.getChannels()),
                entity.getStatus(),
                entity.getReadAt(),
                entity.getCreatedAt(),
                entity.getSentAt()
        );
    }
}
