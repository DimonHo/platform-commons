package com.platformcommons.notification.service;

import com.platformcommons.notification.domain.NotificationTemplate;

import java.util.List;

/**
 * 通知模板服务接口。
 */
public interface NotificationTemplateService {

    /**
     * 创建模板。
     */
    NotificationTemplate createTemplate(String code, String name, String category,
                                        String titleTemplate, String contentTemplate,
                                        String defaultChannels, Boolean enabled);

    /**
     * 更新模板。
     */
    NotificationTemplate updateTemplate(Long id, String name, String titleTemplate,
                                        String contentTemplate, String defaultChannels, Boolean enabled);

    /**
     * 按编码查询。
     */
    NotificationTemplate getByCode(String code);

    /**
     * 列出全部模板。
     */
    List<NotificationTemplate> listAll();

    /**
     * 启用/禁用模板。
     */
    NotificationTemplate toggleEnabled(Long id, boolean enabled);
}
