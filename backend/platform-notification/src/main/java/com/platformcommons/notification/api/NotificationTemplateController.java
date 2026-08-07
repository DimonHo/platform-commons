package com.platformcommons.notification.api;

import com.platformcommons.common.util.RecordUtils;
import com.platformcommons.notification.api.dto.NotificationTemplateResponse;
import com.platformcommons.notification.api.dto.SaveTemplateRequest;
import com.platformcommons.notification.domain.NotificationTemplate;
import com.platformcommons.notification.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * 通知模板管理对外接口。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "通知模板", description = "模板创建、查询、启用/禁用管理")
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    /**
     * 创建模板。
     */
    @PostMapping("/api/notification-templates")
    public NotificationTemplateResponse create(@Valid @RequestBody SaveTemplateRequest request) {
        log.info("创建通知模板：code={}", request.code());
        NotificationTemplate t = templateService.createTemplate(
                request.code(), request.name(), request.category(),
                request.titleTemplate(), request.contentTemplate(),
                request.defaultChannels(), request.enabled()
        );
        return RecordUtils.copy(t, NotificationTemplateResponse.class);
    }

    /**
     * 更新模板。
     */
    @PutMapping("/api/notification-templates/{id}")
    public NotificationTemplateResponse update(@PathVariable Long id,
                                                @RequestBody SaveTemplateRequest request) {
        log.info("更新通知模板：id={}", id);
        NotificationTemplate t = templateService.updateTemplate(
                id, request.name(), request.titleTemplate(),
                request.contentTemplate(), request.defaultChannels(), request.enabled()
        );
        return RecordUtils.copy(t, NotificationTemplateResponse.class);
    }

    /**
     * 按编码查询模板。
     */
    @GetMapping("/api/notification-templates/{code}")
    public NotificationTemplateResponse getByCode(@PathVariable String code) {
        return RecordUtils.copy(templateService.getByCode(code), NotificationTemplateResponse.class);
    }

    /**
     * 列出全部模板。
     */
    @GetMapping("/api/notification-templates")
    public List<NotificationTemplateResponse> listAll() {
        return templateService.listAll().stream()
                .map(t -> RecordUtils.copy(t, NotificationTemplateResponse.class))
                .toList();
    }

    /**
     * 启用/禁用模板。
     */
    @PutMapping("/api/notification-templates/{id}/toggle")
    public NotificationTemplateResponse toggle(@PathVariable Long id,
                                                @RequestParam boolean enabled) {
        log.info("切换模板状态：id={}, enabled={}", id, enabled);
        return RecordUtils.copy(templateService.toggleEnabled(id, enabled), NotificationTemplateResponse.class);
    }

}
