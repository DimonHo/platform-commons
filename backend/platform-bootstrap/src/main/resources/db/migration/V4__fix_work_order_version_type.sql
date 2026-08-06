-- V4: work_order.version 类型对齐 @Version Long（int4 → int8）
ALTER TABLE work_order ALTER COLUMN version TYPE BIGINT;
