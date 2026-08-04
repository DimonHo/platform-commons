-- =====================================================
-- 公地引擎 · 平台扩展 Schema（V2）
-- 对应多身份、钱包、支付网关、提现、业务订单、消息通知、抢单广播
-- PostgreSQL · MVP 阶段无需向后兼容
-- =====================================================

-- ─────────────────────────────────────────────────────
-- Part 1：ALTER 已有表
-- ─────────────────────────────────────────────────────

-- 1. member 表：删除无效的 roles 字段（已被 member_role 替代）
ALTER TABLE member DROP COLUMN IF EXISTS roles;

-- 2. payment_ledger_event 表：增加渠道归属与订单关联
ALTER TABLE payment_ledger_event
    ADD COLUMN member_id    BIGINT,
    ADD COLUMN channel_code VARCHAR(32),
    ADD COLUMN order_no     VARCHAR(64);

-- ─────────────────────────────────────────────────────
-- Part 2：多身份体系（platform-identity）
-- ─────────────────────────────────────────────────────

-- 27. 成员角色注册表（MemberRoleEntity）
CREATE TABLE member_role (
    id             BIGSERIAL    PRIMARY KEY,
    member_id      BIGINT       NOT NULL,
    role_type      VARCHAR(32)  NOT NULL,          -- MEMBER / WORKER / MERCHANT / ADMIN / REVIEWER
    status         VARCHAR(16)  NOT NULL,          -- PENDING / ACTIVE / SUSPENDED / REVOKED
    applied_at     TIMESTAMPTZ  NOT NULL,
    activated_at   TIMESTAMPTZ,
    suspended_at   TIMESTAMPTZ,
    suspend_reason VARCHAR(256),
    reviewer_id    BIGINT,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_member_role UNIQUE (member_id, role_type)
);

-- 28. 劳动者扩展档案（WorkerProfileEntity）
CREATE TABLE worker_profile (
    id                 BIGSERIAL       PRIMARY KEY,
    member_id          BIGINT          NOT NULL,
    service_categories VARCHAR(256)    NOT NULL,   -- RIDE_HAIL,DELIVERY,REPAIR,CLEANING
    service_radius_m   INTEGER         NOT NULL DEFAULT 5000,
    vehicle_type       VARCHAR(32),                -- SEDAN / SUV / EBIKE / TRICYCLE / NONE
    vehicle_plate      VARCHAR(32),
    skills             VARCHAR(256),
    max_concurrent     INTEGER         NOT NULL DEFAULT 1,
    rating             DOUBLE PRECISION NOT NULL DEFAULT 5.0,
    total_completed    INTEGER         NOT NULL DEFAULT 0,
    online_status      VARCHAR(16)     NOT NULL DEFAULT 'OFFLINE', -- ONLINE / OFFLINE / BUSY
    bio                VARCHAR(512),
    CONSTRAINT uk_worker_profile_member UNIQUE (member_id)
);

-- 29. 商家扩展档案（MerchantProfileEntity）
CREATE TABLE merchant_profile (
    id                 BIGSERIAL    PRIMARY KEY,
    member_id          BIGINT       NOT NULL,
    shop_name          VARCHAR(128) NOT NULL,
    shop_category      VARCHAR(64)  NOT NULL,      -- CATERING / RETAIL / REPAIR / ...
    business_license   VARCHAR(64),
    license_photo_url  VARCHAR(512),
    shop_address       VARCHAR(256) NOT NULL,
    shop_lat           DOUBLE PRECISION,
    shop_lng           DOUBLE PRECISION,
    business_hours     VARCHAR(128),               -- 09:00-22:00
    delivery_radius_m  INTEGER      NOT NULL DEFAULT 3000,
    rating             DOUBLE PRECISION NOT NULL DEFAULT 5.0,
    shop_status        VARCHAR(16)  NOT NULL DEFAULT 'CLOSED', -- OPEN / CLOSED
    CONSTRAINT uk_merchant_profile_member UNIQUE (member_id)
);

-- ─────────────────────────────────────────────────────
-- Part 3：钱包与流水（platform-payment）
-- ─────────────────────────────────────────────────────

-- 12. 钱包（WalletEntity）
CREATE TABLE wallet (
    id             BIGSERIAL     PRIMARY KEY,
    member_id      BIGINT        NOT NULL,
    balance        DECIMAL(19,4) NOT NULL DEFAULT 0,
    frozen_amount  DECIMAL(19,4) NOT NULL DEFAULT 0,
    status         VARCHAR(16)   NOT NULL,         -- ACTIVE / FROZEN / CLOSED
    created_at     TIMESTAMPTZ   NOT NULL,
    updated_at     TIMESTAMPTZ   NOT NULL,
    CONSTRAINT uk_wallet_member UNIQUE (member_id)
);

-- 13. 钱包流水（WalletTransactionEntity）
CREATE TABLE wallet_transaction (
    id              BIGSERIAL     PRIMARY KEY,
    wallet_id       BIGINT        NOT NULL,
    member_id       BIGINT        NOT NULL,
    transaction_no  VARCHAR(64)   NOT NULL,
    direction       VARCHAR(8)    NOT NULL,        -- IN / OUT
    amount          DECIMAL(19,4) NOT NULL,
    balance_after   DECIMAL(19,4) NOT NULL,
    business_type   VARCHAR(32)   NOT NULL,        -- RECHARGE / WITHDRAW / REFUND / MUTUAL_CLAIM / SETTLE
    ref_type        VARCHAR(32),                   -- Proposal / MutualClaim / PaymentEvent / WorkOrder
    ref_id          VARCHAR(64),
    remark          VARCHAR(256),
    created_at      TIMESTAMPTZ   NOT NULL,
    CONSTRAINT uk_wal_txn_no UNIQUE (transaction_no)
);

-- ─────────────────────────────────────────────────────
-- Part 4：实名认证、地址簿、银行卡（platform-identity / payment）
-- ─────────────────────────────────────────────────────

-- 14. 地址簿（AddressEntity）
CREATE TABLE address (
    id            BIGSERIAL    PRIMARY KEY,
    member_id     BIGINT       NOT NULL,
    label         VARCHAR(32),                    -- 家 / 公司 / ...
    receiver_name VARCHAR(64)  NOT NULL,
    phone         VARCHAR(20)  NOT NULL,
    province      VARCHAR(32)  NOT NULL,
    city          VARCHAR(32)  NOT NULL,
    district      VARCHAR(32),
    detail        VARCHAR(256) NOT NULL,
    latitude      DOUBLE PRECISION,
    longitude     DOUBLE PRECISION,
    is_default    BOOLEAN      NOT NULL DEFAULT false,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL
);

-- 15. 实名认证（IdentityVerificationEntity）
CREATE TABLE identity_verification (
    id                   BIGSERIAL    PRIMARY KEY,
    member_id            BIGINT       NOT NULL,
    real_name            VARCHAR(64)  NOT NULL,
    id_card_type         VARCHAR(16)  NOT NULL DEFAULT 'ID_CARD',
    id_card_no_enc       VARCHAR(256) NOT NULL,    -- 加密存储
    id_card_no_masked    VARCHAR(32)  NOT NULL,    -- 脱敏展示
    status               VARCHAR(16)  NOT NULL,    -- PENDING / APPROVED / REJECTED
    verification_channel VARCHAR(32),
    face_verified        BOOLEAN      NOT NULL DEFAULT false,
    submitted_at         TIMESTAMPTZ  NOT NULL,
    reviewed_at          TIMESTAMPTZ,
    reviewer_id          BIGINT,
    CONSTRAINT uk_identity_member UNIQUE (member_id)
);

-- 16. 银行卡（BankCardEntity）
CREATE TABLE bank_card (
    id             BIGSERIAL    PRIMARY KEY,
    member_id      BIGINT       NOT NULL,
    holder_name    VARCHAR(64)  NOT NULL,
    card_no_enc    VARCHAR(256) NOT NULL,          -- 加密
    card_no_masked VARCHAR(32)  NOT NULL,          -- 脱敏 ****1234
    bank_name      VARCHAR(64),
    card_type      VARCHAR(16)  NOT NULL DEFAULT 'DEBIT', -- DEBIT / CREDIT
    reserved_phone VARCHAR(20),
    external_token VARCHAR(128),                   -- 第三方支付绑卡 token
    is_default     BOOLEAN      NOT NULL DEFAULT false,
    status         VARCHAR(16)  NOT NULL,          -- ACTIVE / UNBOUND
    bound_at       TIMESTAMPTZ  NOT NULL
);

-- ─────────────────────────────────────────────────────
-- Part 5：统一支付网关（platform-payment）
-- ─────────────────────────────────────────────────────

-- 17. 统一支付订单（PaymentOrderEntity）
CREATE TABLE payment_order (
    id            BIGSERIAL     PRIMARY KEY,
    order_no      VARCHAR(64)   NOT NULL,
    member_id     BIGINT        NOT NULL,
    direction     VARCHAR(8)    NOT NULL,          -- PAY（支付）/ RECHARGE（充值）
    amount        DECIMAL(19,4) NOT NULL,
    business_type VARCHAR(32)   NOT NULL,          -- MUTUAL_CLAIM / PROPOSAL / TOPUP / SETTLE
    ref_type      VARCHAR(32),
    ref_id        VARCHAR(64),
    status        VARCHAR(16)   NOT NULL,          -- PENDING / PAID / FAILED / REFUNDED
    expire_at     TIMESTAMPTZ,
    created_at    TIMESTAMPTZ   NOT NULL,
    paid_at       TIMESTAMPTZ,
    CONSTRAINT uk_pay_order_no UNIQUE (order_no)
);

-- 18. 支付渠道路由（PaymentChannelRouteEntity）
CREATE TABLE payment_channel_route (
    id                BIGSERIAL   PRIMARY KEY,
    payment_order_id  BIGINT      NOT NULL,
    channel_code      VARCHAR(32) NOT NULL,        -- WECHAT_PAY / ALIPAY / UNIONPAY / BANK_TRANSFER
    channel_merchant  VARCHAR(64),
    channel_order_no  VARCHAR(64),                 -- 渠道侧订单号
    channel_resp_code VARCHAR(16),
    channel_resp_msg  VARCHAR(256),
    status            VARCHAR(16) NOT NULL,        -- PENDING / SUCCESS / FAIL / CLOSED
    attempt_count     INTEGER     NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL
);

-- 19. 提现申请（WithdrawalRequestEntity）
CREATE TABLE withdrawal_request (
    id            BIGSERIAL     PRIMARY KEY,
    request_no    VARCHAR(64)   NOT NULL,
    member_id     BIGINT        NOT NULL,
    wallet_id     BIGINT        NOT NULL,
    bank_card_id  BIGINT        NOT NULL,
    amount        DECIMAL(19,4) NOT NULL,
    fee           DECIMAL(19,4) NOT NULL DEFAULT 0,
    status        VARCHAR(16)   NOT NULL,          -- PENDING / APPROVED / REJECTED / PROCESSING / SUCCESS / FAILED
    risk_score    INTEGER,
    reject_reason VARCHAR(256),
    applied_at    TIMESTAMPTZ   NOT NULL,
    reviewed_at   TIMESTAMPTZ,
    reviewer_id   BIGINT,
    completed_at  TIMESTAMPTZ,
    CONSTRAINT uk_withdraw_req_no UNIQUE (request_no)
);

-- 20. 提现渠道记录（WithdrawalRecordEntity）
CREATE TABLE withdrawal_record (
    id                    BIGSERIAL   PRIMARY KEY,
    withdrawal_request_id BIGINT      NOT NULL,
    channel_code          VARCHAR(32) NOT NULL,    -- WECHAT_PAY / ALIPAY / UNIONPAY / BANK_TRANSFER
    channel_merchant      VARCHAR(64),
    channel_transfer_no   VARCHAR(64),             -- 渠道侧转账单号
    channel_resp_code     VARCHAR(16),
    channel_resp_msg      VARCHAR(256),
    status                VARCHAR(16) NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL,
    updated_at            TIMESTAMPTZ NOT NULL
);

-- ─────────────────────────────────────────────────────
-- Part 6：业务订单与流转（platform-matching）
-- ─────────────────────────────────────────────────────

-- 21. 业务订单（WorkOrderEntity）
CREATE TABLE work_order (
    id            BIGSERIAL     PRIMARY KEY,
    order_no      VARCHAR(64)   NOT NULL,
    order_type    VARCHAR(32)   NOT NULL,          -- LABOR / SERVICE / DELIVERY / RIDE_HAIL / MUTUAL_ASSIST
    title         VARCHAR(128)  NOT NULL,
    description   TEXT,
    member_id     BIGINT        NOT NULL,          -- 需求方 / 发起人
    worker_id     BIGINT,                         -- 接单劳动者
    chamber       VARCHAR(32),                    -- 所属议事会
    amount        DECIMAL(19,4) NOT NULL,
    status        VARCHAR(16)   NOT NULL,          -- CREATED/DISPATCHED/ACCEPTED/IN_PROGRESS/SUBMITTED/APPROVED/REJECTED/SETTLED/CLOSED/CANCELLED/DISPUTED
    priority      VARCHAR(16)   NOT NULL DEFAULT 'NORMAL',
    location_lat  DOUBLE PRECISION,
    location_lng  DOUBLE PRECISION,
    scheduled_at  TIMESTAMPTZ,                    -- 预约服务时间
    accepted_at   TIMESTAMPTZ,
    started_at    TIMESTAMPTZ,
    submitted_at  TIMESTAMPTZ,                    -- 提交验收
    completed_at  TIMESTAMPTZ,
    cancelled_at  TIMESTAMPTZ,
    cancel_reason VARCHAR(256),
    created_at    TIMESTAMPTZ   NOT NULL,
    updated_at    TIMESTAMPTZ   NOT NULL,
    CONSTRAINT uk_work_order_no UNIQUE (order_no)
);

-- 22. 订单状态流转记录（OrderTransitionEntity）
CREATE TABLE order_transition (
    id              BIGSERIAL    PRIMARY KEY,
    order_id        BIGINT       NOT NULL,
    from_status     VARCHAR(16)  NOT NULL,
    to_status       VARCHAR(16)  NOT NULL,
    action          VARCHAR(32)  NOT NULL,         -- DISPATCH/ACCEPT/START/SUBMIT/APPROVE/REJECT/CANCEL/DISPUTE/SETTLE
    operator_id     BIGINT       NOT NULL,
    operator_role   VARCHAR(32),                   -- MEMBER / WORKER / ADMIN / SYSTEM
    remark          VARCHAR(256),
    attachment_urls VARCHAR(2048),                 -- 验收照片 / 凭证
    created_at      TIMESTAMPTZ  NOT NULL
);

-- ─────────────────────────────────────────────────────
-- Part 7：消息通知（platform-notification）
-- ─────────────────────────────────────────────────────

-- 23. 消息通知（NotificationEntity）
CREATE TABLE notification (
    id             BIGSERIAL    PRIMARY KEY,
    recipient_id   BIGINT       NOT NULL,
    recipient_role VARCHAR(32),                    -- MERCHANT / WORKER / MEMBER / ADMIN
    category       VARCHAR(32)  NOT NULL,          -- ORDER / PAYMENT / GOVERNANCE / SYSTEM
    title          VARCHAR(128) NOT NULL,
    content        TEXT         NOT NULL,
    ref_type       VARCHAR(32),                    -- WorkOrder / PaymentOrder / Proposal / DispatchBroadcast
    ref_id         VARCHAR(64),
    channels       VARCHAR(64)  NOT NULL,          -- IN_APP / IN_APP,SMS / IN_APP,PUSH
    status         VARCHAR(16)  NOT NULL,          -- PENDING / SENT / DELIVERED / READ / FAILED
    read_at        TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL,
    sent_at        TIMESTAMPTZ
);

-- 24. 通知模板（NotificationTemplateEntity）
CREATE TABLE notification_template (
    id               BIGSERIAL    PRIMARY KEY,
    code             VARCHAR(64)  NOT NULL,        -- ORDER_CREATED_MERCHANT / PAYMENT_SUCCESS / ...
    name             VARCHAR(128) NOT NULL,
    category         VARCHAR(32)  NOT NULL,
    title_template   VARCHAR(256) NOT NULL,        -- 含 {orderNo} {amount} 等占位符
    content_template TEXT         NOT NULL,
    default_channels VARCHAR(64)  NOT NULL,        -- IN_APP,SMS
    enabled          BOOLEAN      NOT NULL DEFAULT true,
    created_at       TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_notif_tpl_code UNIQUE (code)
);

-- ─────────────────────────────────────────────────────
-- Part 8：抢单广播（platform-matching）
-- ─────────────────────────────────────────────────────

-- 25. 派单广播 / 抢单池（DispatchBroadcastEntity）
CREATE TABLE dispatch_broadcast (
    id             BIGSERIAL       PRIMARY KEY,
    broadcast_no   VARCHAR(64)     NOT NULL,
    order_id       BIGINT          NOT NULL,       -- → work_order
    order_type     VARCHAR(32)     NOT NULL,       -- RIDE_HAIL / DELIVERY / LABOR / MUTUAL_ASSIST
    broadcast_type VARCHAR(16)     NOT NULL,       -- GRAB（抢单）/ ASSIGN（系统指派）
    center_lat     DOUBLE PRECISION NOT NULL,      -- 广播中心点（用户位置）
    center_lng     DOUBLE PRECISION NOT NULL,
    radius_meters  INTEGER         NOT NULL,       -- 广播半径
    target_count   INTEGER         NOT NULL DEFAULT 1, -- 需要匹配人数
    grabbed_count  INTEGER         NOT NULL DEFAULT 0,
    status         VARCHAR(16)     NOT NULL,       -- BROADCASTING / GRABBED / EXPIRED / CANCELLED
    expire_at      TIMESTAMPTZ     NOT NULL,       -- 抢单截止时间
    created_at     TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uk_dispatch_bcast_no UNIQUE (broadcast_no)
);

-- 26. 抢单记录（DispatchGrabRecordEntity）
CREATE TABLE dispatch_grab_record (
    id             BIGSERIAL       PRIMARY KEY,
    broadcast_id   BIGINT          NOT NULL,
    worker_id      BIGINT          NOT NULL,
    worker_lat     DOUBLE PRECISION NOT NULL,
    worker_lng     DOUBLE PRECISION NOT NULL,
    distance_meters INTEGER,                       -- 接单时距用户距离
    status         VARCHAR(16)     NOT NULL,       -- PENDING / WIN / LOSE
    grabbed_at     TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uk_grab_bcast_worker UNIQUE (broadcast_id, worker_id) -- 同一广播每人只能抢一次
);
