-- =====================================================
-- 公地引擎 · 初始 Schema（V1）
-- 对应 11 个 JPA Entity，PostgreSQL
-- =====================================================

-- 1. 成员（platform-identity → MemberEntity）
CREATE TABLE member (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL,
    phone       VARCHAR(20)  NOT NULL,
    roles       VARCHAR(64)  NOT NULL,
    status      VARCHAR(16)  NOT NULL,
    registered_at TIMESTAMP   NOT NULL,
    labor_shares INTEGER,
    CONSTRAINT uk_member_phone UNIQUE (phone)
);

-- 2. 提案（platform-governance → ProposalEntity）
CREATE TABLE proposal (
    id             BIGSERIAL    PRIMARY KEY,
    title          VARCHAR(128) NOT NULL,
    description    VARCHAR(2048),
    type           VARCHAR(32)  NOT NULL,
    status         VARCHAR(16)  NOT NULL,
    proposer_id    BIGINT       NOT NULL,
    target_chamber VARCHAR(32),
    voting_start_at TIMESTAMP,
    voting_end_at   TIMESTAMP,
    created_at      TIMESTAMP   NOT NULL
);

-- 3. 投票（platform-governance → VoteEntity）
CREATE TABLE vote (
    id          BIGSERIAL   PRIMARY KEY,
    proposal_id BIGINT      NOT NULL,
    voter_id    BIGINT      NOT NULL,
    choice      VARCHAR(16) NOT NULL,
    voted_at    TIMESTAMP   NOT NULL,
    CONSTRAINT uk_proposal_voter UNIQUE (proposal_id, voter_id)
);

-- 4. 支付账本（platform-payment → LedgerEventEntity）
CREATE TABLE payment_ledger_event (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id        UUID         NOT NULL,
    transaction_id  UUID         NOT NULL,
    event_type      VARCHAR(32)  NOT NULL,
    amount          DECIMAL(19,4),
    occurred_at     TIMESTAMPTZ  NOT NULL
);

-- 5. 互助基金（platform-mutual → MutualClaimEntity）
CREATE TABLE mutual_claim (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    applicant_id   VARCHAR(64)   NOT NULL,
    incident_type  VARCHAR(32)   NOT NULL,
    description    VARCHAR(1024),
    claimed_amount DECIMAL(19,4) NOT NULL,
    evidence_urls  VARCHAR(2048),
    status         VARCHAR(16)   NOT NULL,
    submitted_at   TIMESTAMPTZ   NOT NULL,
    reviewed_at    TIMESTAMPTZ,
    reviewer_id    VARCHAR(64)
);

-- 6. 劳动者位置（platform-matching → WorkerLocationEntity）
CREATE TABLE worker_location (
    worker_id        VARCHAR(64)    PRIMARY KEY,
    latitude         DOUBLE PRECISION NOT NULL,
    longitude        DOUBLE PRECISION NOT NULL,
    active_orders    INTEGER        NOT NULL,
    rating           DOUBLE PRECISION,
    registration_days INTEGER       NOT NULL,
    updated_at       TIMESTAMPTZ    NOT NULL
);

-- 7. AI 审议（platform-ai-supervision → AiReviewRecordEntity）
CREATE TABLE ai_review_records (
    id                BIGSERIAL    PRIMARY KEY,
    review_id         VARCHAR(32)  NOT NULL,
    mandatory_item    VARCHAR(64)  NOT NULL,
    proposal          TEXT         NOT NULL,
    status            VARCHAR(32)  NOT NULL,
    consensus_reached BOOLEAN,
    dissent           TEXT,
    CONSTRAINT uk_ai_review_review_id UNIQUE (review_id)
);

-- 8. 技术部署（platform-tech-governance → DeploymentRecordEntity）
CREATE TABLE tech_deployment_records (
    id                  BIGSERIAL    PRIMARY KEY,
    deployment_id       VARCHAR(64)  NOT NULL,
    commit_hash         VARCHAR(128),
    build_artifact_hash VARCHAR(128),
    config_digest       VARCHAR(128),
    deployed_by         VARCHAR(64),
    verification_status VARCHAR(32)  NOT NULL,
    CONSTRAINT uk_tech_deployment_id UNIQUE (deployment_id)
);

-- 9. 融资记录（platform-finance → FinancingRecordEntity）
CREATE TABLE finance_financing_records (
    id             BIGSERIAL     PRIMARY KEY,
    record_id      VARCHAR(32)   NOT NULL,
    amount         DECIMAL(19,2) NOT NULL,
    financing_type VARCHAR(64),
    repayment_cap  DECIMAL(19,2) NOT NULL,
    no_governance  BOOLEAN       NOT NULL,
    disclosed_at   VARCHAR(32),
    CONSTRAINT uk_finance_record_id UNIQUE (record_id)
);

-- 10. 争议（platform-dispute → DisputeEntity）
CREATE TABLE dispute_records (
    id          BIGSERIAL    PRIMARY KEY,
    dispute_id  VARCHAR(32)  NOT NULL,
    filed_by    VARCHAR(64)  NOT NULL,
    subject     VARCHAR(256) NOT NULL,
    description TEXT         NOT NULL,
    level       VARCHAR(32)  NOT NULL,
    status      VARCHAR(32)  NOT NULL,
    resolution  TEXT,
    filed_at    VARCHAR(32),
    CONSTRAINT uk_dispute_dispute_id UNIQUE (dispute_id)
);

-- 11. 预警（platform-early-warning → AlertEntity）
CREATE TABLE early_warning_alert (
    id                     UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    level                  VARCHAR(16)  NOT NULL,
    category               VARCHAR(32)  NOT NULL,
    red_line_code          VARCHAR(16),
    title                  VARCHAR(128) NOT NULL,
    description            VARCHAR(1024),
    source_metric          VARCHAR(128),
    threshold              VARCHAR(128),
    auto_measure_triggered BOOLEAN      NOT NULL,
    acknowledged           BOOLEAN      NOT NULL,
    acknowledged_by        VARCHAR(64),
    triggered_at           TIMESTAMPTZ  NOT NULL,
    cleared_at             TIMESTAMPTZ
);
