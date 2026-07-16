#!/bin/bash
# 20250921g 智慧养老后端自动拉起守护
# 仅当本后端进程不存在时启动，绝不杀/碰他人进程；仅作用于自有目录 /root/20250921g
# 注意：若共享 MySQL 未就绪，则跳过启动后端，避免对死库空转重启（MySQL 由系统 systemd 自动恢复）
export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

JAR=/root/20250921g/backend/elderly-care-api-1.0.0.jar
CFG=/root/20250921g/backend/application.yml
LOG=/root/20250921g/backend/backend.log
PORT=8080
MYSQL_PORT=3306

# 后端端口已在监听 -> 健康，退出
if ss -tlnp 2>/dev/null | grep -q ":${PORT} "; then
  exit 0
fi

# java 进程已存在（含启动中）-> 退出，避免重复拉起
if pgrep -f "java.*elderly-care-api-1.0.0.jar" >/dev/null 2>&1; then
  exit 0
fi

# 共享 MySQL 未就绪 -> 不空转重启后端，等下次 cron 再检查
if ! ss -tlnp 2>/dev/null | grep -q ":${MYSQL_PORT} "; then
  echo "[$(date '+%F %T')] keepalive: MySQL(${MYSQL_PORT}) 未就绪，跳过后端启动，等待下次检查" >> "$LOG"
  exit 0
fi

cd /root/20250921g/backend
echo "[$(date '+%F %T')] keepalive: 后端未运行，尝试启动..." >> "$LOG"
# setsid 完全脱离会话；-Xms128m -Xmx256m 控制小内存下占用，降低再次 OOM 概率
setsid java -Xms128m -Xmx256m -jar "$JAR" --spring.config.location="$CFG" >> "$LOG" 2>&1 &
