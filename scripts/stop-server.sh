#!/usr/bin/env bash

PROJECT_ROOT="/home/ec2-user/routine-connect"
CURRENT_PID=$(pgrep -fla java | grep RoutineConnect | awk '{print $1}')
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date +%c)

# 프로세스가 켜져 있으면 종료
if [ -n "$CURRENT_PID" ]; then
  echo "$TIME_NOW > 실행중인 $CURRENT_PID 애플리케이션 종료 " >> $DEPLOY_LOG
  kill -15 $CURRENT_PID
fi