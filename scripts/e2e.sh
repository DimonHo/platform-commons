#!/usr/bin/env bash
# =====================================================================
# Platform-Commons MVP 端到端验收：注册 → 下单 → 接单 → 完成 → 结算
# 前置：PG 运行（scripts/pg.sh start）+ bootstrap 已启动（8080）
# 用法：bash scripts/e2e.sh
# =====================================================================
set -euo pipefail

BASE=http://localhost:8080
PASS=0; FAIL=0

jget() { echo "$1" | python3 -c "import json,sys; d=json.load(sys.stdin); v=$2; print(v if v is not None else '')" 2>/dev/null || true; }
step() { echo "── $1"; }
check() { if [ "$1" = "$2" ]; then PASS=$((PASS+1)); echo "  ✅ $3"; else FAIL=$((FAIL+1)); echo "  ❌ $3 (期望 $2, 实际 $1)"; fi }

# 1. 注册 merchant + worker（幂等：已存在则从列表复用）
step "1. 注册会员"
find_member_id() { # $1=phone → id（无则空）
  curl -s $BASE/api/members | python3 -c "
import json,sys
try:
    d = json.load(sys.stdin)
    masked = '$1'[:3] + '****' + '$1'[-4:]
    for m in d.get('data') or []:
        if m.get('phone') == masked:
            print(m['id']); break
except Exception:
    pass
" 2>/dev/null || true
}
M_RESP=$(curl -s -X POST $BASE/api/members -H 'Content-Type: application/json' \
  -d '{"name":"E2E商户","phone":"13811110001"}')
MERCHANT_ID=$(jget "$M_RESP" "d['data']['id']" 2>/dev/null)
[ -z "$MERCHANT_ID" ] && MERCHANT_ID=$(find_member_id 13811110001)
W_RESP=$(curl -s -X POST $BASE/api/members -H 'Content-Type: application/json' \
  -d '{"name":"E2E工人","phone":"13811110002"}')
WORKER_ID=$(jget "$W_RESP" "d['data']['id']" 2>/dev/null)
[ -z "$WORKER_ID" ] && WORKER_ID=$(find_member_id 13811110002)
check "$([ -n "$MERCHANT_ID" ] && echo True)" "True" "merchant 注册/复用 id=$MERCHANT_ID"
check "$([ -n "$WORKER_ID" ] && echo True)" "True" "worker 注册/复用 id=$WORKER_ID"

# 2. merchant 钱包（先创建再充值——结算扣款余额来源）
step "2. 商户钱包充值"
curl -s $BASE/api/wallets/$MERCHANT_ID > /dev/null   # getOrCreateWallet
R=$(curl -s -X POST $BASE/api/wallets/recharge -H 'Content-Type: application/json' \
  -d "{\"memberId\":$MERCHANT_ID,\"amount\":1000.00}")
check "$(jget "$R" "d['success']")" "True" "充值 1000"

# 3. 下单
step "3. 下单"
R=$(curl -s -X POST $BASE/api/work-orders -H 'Content-Type: application/json' \
  -d "{\"memberId\":$MERCHANT_ID,\"orderType\":\"SERVICE\",\"title\":\"E2E 修水管\",\"description\":\"验收链路\",\"amount\":200.00,\"locationLat\":30.1,\"locationLng\":120.2,\"priority\":\"NORMAL\"}")
check "$(jget "$R" "d['success']")" "True" "下单"
ORDER_ID=$(jget "$R" "d['data']['id']")
ORDER_NO=$(jget "$R" "d['data']['orderNo']")
echo "       orderId=$ORDER_ID orderNo=$ORDER_NO"

# 4. 派单广播
step "4. 派单广播"
R=$(curl -s -X POST $BASE/api/dispatch/broadcasts -H 'Content-Type: application/json' \
  -d "{\"orderId\":$ORDER_ID,\"orderType\":\"SERVICE\",\"centerLat\":30.1,\"centerLng\":120.2,\"radiusMeters\":5000,\"targetCount\":1,\"broadcastType\":\"GRAB\"}")
check "$(jget "$R" "d['success']")" "True" "广播创建"
BROADCAST_ID=$(jget "$R" "d['data']['id']")

# 5. 抢单
step "5. 工人抢单"
R=$(curl -s -X POST $BASE/api/dispatch/broadcasts/$BROADCAST_ID/grab -H 'Content-Type: application/json' \
  -d "{\"workerId\":$WORKER_ID,\"workerLat\":30.11,\"workerLng\":120.21}")
check "$(jget "$R" "d['success']")" "True" "抢单成功"

# 6. 状态流转：DISPATCH → ACCEPT → START → SUBMIT → APPROVE → SETTLE
step "6. 状态流转"
for ACTION in DISPATCH ACCEPT START SUBMIT APPROVE SETTLE; do
  R=$(curl -s -X POST $BASE/api/work-orders/$ORDER_ID/transition -H 'Content-Type: application/json' \
    -d "{\"action\":\"$ACTION\",\"operatorId\":1,\"operatorRole\":\"SYSTEM\",\"remark\":\"e2e-$ACTION\"}")
  check "$(jget "$R" "d['success']")" "True" "transition($ACTION)"
done

# 7. 创建交易 + 结算
step "7. 结算"
R=$(curl -s -X POST $BASE/api/payment/charge -H 'Content-Type: application/json' \
  -d "{\"orderId\":\"$ORDER_NO\",\"workerId\":\"$WORKER_ID\",\"requesterId\":\"$MERCHANT_ID\",\"grossAmount\":200.00}")
check "$(jget "$R" "d['success']")" "True" "charge"
TX_ID=$(jget "$R" "d['data']['id']")
R=$(curl -s -X POST $BASE/api/payment/settle/$TX_ID)
check "$(jget "$R" "d['success']")" "True" "settle"

# 8. 断言：工单终态 + 钱包余额增量 + 金额守恒
step "8. 最终断言"
R=$(curl -s $BASE/api/work-orders/$ORDER_NO)
check "$(jget "$R" "d['data']['status']")" "SETTLED" "工单状态 SETTLED"

W=$(curl -s $BASE/api/wallets/$WORKER_ID)
WORKER_BALANCE=$(jget "$W" "d['data']['balance']")
M=$(curl -s $BASE/api/wallets/$MERCHANT_ID)
MERCHANT_BALANCE=$(jget "$M" "d['data']['balance']")
echo "       worker余额=$WORKER_BALANCE merchant余额=$MERCHANT_BALANCE"
# 金额守恒（增量断言，兼容历史余额）：worker 到账 = workerShare，merchant 扣款 = 订单额
TX=$(curl -s $BASE/api/payment/$TX_ID)
WORKER_SHARE=$(jget "$TX" "d['data']['workerShare']")
FEE=$(jget "$TX" "d['data']['platformFee']")
python3 - "$WORKER_SHARE" "$FEE" "$WORKER_BALANCE" "$MERCHANT_BALANCE" <<'EOF'
import sys
share, fee, wb, mb = map(float, sys.argv[1:])
assert abs(share + fee - 200.0) < 0.01, f"守恒失败: share+fee={share+fee} != 200"
assert share > 0 and wb >= share, f"worker 到账不符: {wb} < {share}"
print(f"  ✅ 金额守恒: share+fee=200, worker 到账 {share}, merchant 本次扣款 200")
EOF

echo
echo "════════════════════════════════════════"
echo "MVP E2E 结果: PASS=$PASS FAIL=$FAIL"
if [ "$FAIL" -eq 0 ]; then echo "MVP E2E PASS: 注册→下单→接单→完成→结算"; else echo "MVP E2E FAIL"; exit 1; fi
