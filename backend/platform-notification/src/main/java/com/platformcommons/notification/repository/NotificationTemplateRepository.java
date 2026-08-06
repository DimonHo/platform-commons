package com.platformcommons.notification.repository;

import com.platformcommons.notification.repository.entity.NotificationTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 通知模板 Repository。
 */
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplateEntity, Long> {

    Optional<NotificationTemplateEntity> findByCode(String code);

    Optional<NotificationTemplateEntity> findByCodeAndEnabledTrue(String code);
}
