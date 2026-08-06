package com.platformcommons.notification.repository;

import com.platformcommons.notification.domain.NotificationCategory;
import com.platformcommons.notification.domain.NotificationStatus;
import com.platformcommons.notification.repository.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 通知 Repository。
 */
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    Page<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    List<NotificationEntity> findByRecipientIdAndStatusOrderByCreatedAtDesc(Long recipientId, NotificationStatus status);

    long countByRecipientIdAndStatus(Long recipientId, NotificationStatus status);

    List<NotificationEntity> findByCategoryAndStatus(NotificationCategory category, NotificationStatus status);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.status = :status, n.readAt = :readAt WHERE n.id = :id")
    void updateStatusAndReadAt(@Param("id") Long id, @Param("status") NotificationStatus status, @Param("readAt") java.time.Instant readAt);
}
