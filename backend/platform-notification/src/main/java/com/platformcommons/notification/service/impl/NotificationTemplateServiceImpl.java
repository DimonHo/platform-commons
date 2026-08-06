package com.platformcommons.notification.service.impl;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.notification.domain.NotificationCategory;
import com.platformcommons.notification.domain.NotificationTemplate;
import com.platformcommons.notification.repository.NotificationTemplateRepository;
import com.platformcommons.notification.repository.entity.NotificationTemplateEntity;
import com.platformcommons.notification.service.NotificationTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * 通知模板服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationTemplate createTemplate(String code, String name, String category,
                                                String titleTemplate, String contentTemplate,
                                                String defaultChannels, Boolean enabled) {
        log.info("创建通知模板：code={}, name={}", code, name);

        templateRepository.findByCode(code).ifPresent(t -> {
            throw new BusinessException(ResultCode.DATA_DUPLICATED, "模板编码已存在: " + code);
        });

        NotificationTemplateEntity entity = new NotificationTemplateEntity();
        entity.setCode(code);
        entity.setName(name);
        entity.setCategory(parseCategory(category));
        entity.setTitleTemplate(titleTemplate);
        entity.setContentTemplate(contentTemplate);
        entity.setDefaultChannels(defaultChannels);
        entity.setEnabled(enabled != null ? enabled : true);
        entity.setCreatedAt(Instant.now());

        NotificationTemplateEntity saved = templateRepository.save(entity);
        log.info("模板创建成功：id={}, code={}", saved.getId(), code);
        return toDomain(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationTemplate updateTemplate(Long id, String name, String titleTemplate,
                                                String contentTemplate, String defaultChannels, Boolean enabled) {
        log.info("更新通知模板：id={}", id);
        NotificationTemplateEntity entity = requireTemplate(id);

        if (name != null) {
            entity.setName(name);
        }
        if (titleTemplate != null) {
            entity.setTitleTemplate(titleTemplate);
        }
        if (contentTemplate != null) {
            entity.setContentTemplate(contentTemplate);
        }
        if (defaultChannels != null) {
            entity.setDefaultChannels(defaultChannels);
        }
        if (enabled != null) {
            entity.setEnabled(enabled);
        }

        NotificationTemplateEntity saved = templateRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationTemplate getByCode(String code) {
        return templateRepository.findByCode(code)
                .map(NotificationTemplateServiceImpl::toDomain)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "模板不存在: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationTemplate> listAll() {
        return templateRepository.findAll().stream()
                .map(NotificationTemplateServiceImpl::toDomain)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationTemplate toggleEnabled(Long id, boolean enabled) {
        log.info("切换模板状态：id={}, enabled={}", id, enabled);
        NotificationTemplateEntity entity = requireTemplate(id);
        entity.setEnabled(enabled);
        NotificationTemplateEntity saved = templateRepository.save(entity);
        return toDomain(saved);
    }

    // ===== 内部工具 =====

    private NotificationTemplateEntity requireTemplate(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "模板不存在: " + id));
    }

    private static NotificationCategory parseCategory(String name) {
        try {
            return NotificationCategory.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "非法通知分类: " + name);
        }
    }

    private static NotificationTemplate toDomain(NotificationTemplateEntity entity) {
        return new NotificationTemplate(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getCategory(),
                entity.getTitleTemplate(),
                entity.getContentTemplate(),
                entity.getDefaultChannels(),
                entity.getEnabled(),
                entity.getCreatedAt()
        );
    }
}
