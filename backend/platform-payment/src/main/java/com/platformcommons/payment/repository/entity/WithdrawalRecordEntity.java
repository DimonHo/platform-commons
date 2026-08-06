package com.platformcommons.payment.repository.entity;

import com.platformcommons.payment.domain.ChannelCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 提现记录持久化实体。
 *
 * <p>status 字段为 String（按 DDL 定义），不使用枚举。</p>
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "withdrawal_record")
public class WithdrawalRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "withdrawal_request_id", nullable = false)
    private Long withdrawalRequestId;

    @Column(name = "channel_code", length = 32)
    @Enumerated(EnumType.STRING)
    private ChannelCode channelCode;

    @Column(name = "channel_merchant", length = 64)
    private String channelMerchant;

    @Column(name = "channel_transfer_no", length = 64)
    private String channelTransferNo;

    @Column(name = "channel_resp_code", length = 16)
    private String channelRespCode;

    @Column(name = "channel_resp_msg", length = 256)
    private String channelRespMsg;

    @Column(name = "status", length = 16)
    private String status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
