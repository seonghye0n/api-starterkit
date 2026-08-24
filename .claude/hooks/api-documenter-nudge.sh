#!/usr/bin/env bash
# PostToolUse(Edit|Write) 훅: 컨트롤러 파일이 수정되면 api-documenter 서브 에이전트 실행을 유도한다.
set -euo pipefail

input="$(cat)"
file_path="$(printf '%s' "$input" | jq -r '.tool_input.file_path // empty')"

case "$file_path" in
  *domain/*/controller/*Controller.java|*global/file/FileController.java)
    jq -n --arg f "$file_path" '{
      hookSpecificOutput: {
        hookEventName: "PostToolUse",
        additionalContext: ("컨트롤러 파일이 방금 수정되었습니다: " + $f + ". api-documenter 서브 에이전트(.claude/agents/api-documenter.md)를 Agent 툴로 실행해 이 엔드포인트의 Swagger 어노테이션과 README.md \"주요 API\" 표를 최신 상태로 갱신하세요.")
      }
    }'
    ;;
esac
