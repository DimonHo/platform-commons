-- =====================================================================
-- V3: MVP 结算持久化 + 并发控制
-- 1. payment_transaction：PaymentServiceImpl 内存台账 → JPA 持久化
-- 2. work_order.version：抢单并发乐观锁
-- =====================================================================

-- 1. 交易表（对应 payment.domain.Transaction record）
CREATE TABLE payment_transaction (
    id            UUID         PRIMARY KEY,                -- Transaction.id（Java 侧 UUID 生成）
    order_id      VARCHAR(64)  NOT NULL,                   -- 业务订单号（幂等键）
    worker_id     VARCHAR(64),                             -- 劳动者 ID
    requester_id  VARCHAR(64),                             -- 发包方 ID
    gross_amount  DECIMAL(19,4) NOT NULL,                  -- 订单总价
    platform_fee  DECIMAL(19,4) NOT NULL DEFAULT 0,        -- 平台服务费
    worker_share  DECIMAL(19,4) NOT NULL DEFAULT 0,        -- 劳动者返还
    status        VARCHAR(16)  NOT NULL,                   -- PENDING/CHARGED/SETTLED/REFUNDED/FAILED
    rule_version  VARCHAR(32),                             -- 分账规则版本
    created_at    TIMESTAMPTZ  NOT NULL,
    settled_at    TIMESTAMPTZ,
    CONSTRAINT uk_payment_transaction_order UNIQUE (order_id)
);

CREATE INDEX idx_payment_transaction_status ON payment_transaction (status);

-- 2. work_order 乐观锁：并发抢单时防止 grabbed_count 丢失更新
ALTER TABLE work_order ADD COLUMN version INTEGER NOT NULL DEFAULT 0;
