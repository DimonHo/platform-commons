#!/usr/bin/env bash
# 用户态 PostgreSQL 17（zonky embedded binaries）启动/停止脚本
# 用法: bash scripts/pg.sh start|stop|status
set -euo pipefail

PG_BIN=/opt/data/pg17/bin
PG_DATA=/opt/data/pg17/data
PG_LOG=/opt/data/pg17/pg.log

case "${1:-start}" in
  start)
    if [ ! -f "$PG_DATA/postmaster.pid" ]; then
      "$PG_BIN/pg_ctl" -D "$PG_DATA" -l "$PG_LOG" start
      echo "PG started (localhost:5432, user=postgres, auth=trust)"
    else
      echo "PG already running"
    fi
    ;;
  stop)
    "$PG_BIN/pg_ctl" -D "$PG_DATA" stop || true
    echo "PG stopped"
    ;;
  status)
    "$PG_BIN/pg_ctl" -D "$PG_DATA" status || true
    ;;
esac
