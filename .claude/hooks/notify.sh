#!/usr/bin/env bash
# Notification/Stop 훅: Slack Incoming Webhook으로 모바일 푸시 알림을 보낸다.
# 알림 전용 훅이므로 어떤 경우에도 Claude Code 흐름을 막지 않는다(항상 exit 0).
set -uo pipefail   # -e 는 의도적으로 제외: curl/jq 실패가 훅을 죽이지 않도록

# 웹훅 URL 미설정 시 조용히 종료 (설정 전이거나 다른 머신에서 클론한 경우)
[ -n "${SLACK_WEBHOOK_URL:-}" ] || exit 0

input="$(cat)"
event="$(printf '%s' "$input" | jq -r '.hook_event_name // empty')"

# 어느 프로젝트에서 온 알림인지 구분 (여러 세션을 동시에 돌릴 때 필수)
cwd="$(printf '%s' "$input" | jq -r '.cwd // empty')"
project="${cwd##*/}"
[ -n "$project" ] || project="claude-code"

case "$event" in
  Notification)
    # 구버전은 .message, 신버전은 .notification_type 을 준다 → 양쪽 모두 대응
    body="$(printf '%s' "$input" | jq -r '.message // empty')"
    if [ -z "$body" ]; then
      ntype="$(printf '%s' "$input" | jq -r '.notification_type // empty')"
      case "$ntype" in
        permission_prompt) body="도구 실행 권한 승인을 기다리고 있습니다." ;;
        agent_needs_input|elicitation_dialog|elicitation_url_dialog)
                           body="입력이 필요합니다." ;;
        # auth_success, idle_prompt, quota_* 등은 알림 가치가 낮고
        # idle_prompt는 Stop 직후 중복 발화하므로 무시한다
        *) exit 0 ;;
      esac
    fi
    title="🔔 권한/입력 대기"
    ;;
  Stop)
    # 훅이 block한 뒤 재진입한 경우 중복 알림 방지
    [ "$(printf '%s' "$input" | jq -r '.stop_hook_active // false')" = "true" ] && exit 0
    # 공백 정규화 + 200자 컷. jq의 .[0:200]은 문자 단위라 한글도 안전하게 잘린다
    body="$(printf '%s' "$input" \
      | jq -r '(.last_assistant_message // "") | gsub("\\s+"; " ") | .[0:200]')"
    [ -n "$body" ] || body="작업을 마쳤습니다."
    title="✅ 작업 완료"
    ;;
  *) exit 0 ;;
esac

# jq -n 으로 payload 생성 → 따옴표/개행/유니코드를 안전하게 이스케이프.
# Slack 모바일 푸시는 blocks가 아닌 text 필드를 미리보기로 쓰므로 text는 필수다.
payload="$(jq -n --arg t "$title" --arg p "$project" --arg b "$body" \
  '{text: ($t + "  ·  `" + $p + "`\n" + $b)}')"

curl -sS -X POST -H 'Content-Type: application/json' \
     --max-time 5 --data "$payload" "$SLACK_WEBHOOK_URL" >/dev/null 2>&1 || true

exit 0
