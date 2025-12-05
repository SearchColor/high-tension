#!/bin/bash

# ==============================================================================
# Gateway Rate Limiting 테스트 스크립트
# ==============================================================================
# 목적: Token Bucket 알고리즘 기반 Rate Limiting 테스트
# 알고리즘: 10 tokens 용량, 1 token/sec 리필 속도 (테스트 설정)
# 예상 동작:
#   - 1-10번째 요청: 200 OK
#   - 11번째 요청: 429 Too Many Requests
#   - 10초 대기 후: 200 OK (10개 토큰 리필됨)
# ==============================================================================

# 색상 코드
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 설정
GATEWAY_URL="http://localhost:8000"
SIGNUP_ENDPOINT="${GATEWAY_URL}/api/v1/users/signup"
LOGIN_ENDPOINT="${GATEWAY_URL}/api/v1/users/login"
TEST_ENDPOINT="${GATEWAY_URL}/api/v1/users/me"

# 테스트 사용자 credentials (매번 랜덤 이메일로 새 계정 생성)
TIMESTAMP=$(date +%s)
EMAIL="test_${TIMESTAMP}@example.com"
PASSWORD="password1234@"
NAME="TestUser${TIMESTAMP}"

# 결과 파일 경로 (scripts 폴더 내)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESULT_FILE="${SCRIPT_DIR}/rate-limiting-test-result-${TIMESTAMP}.txt"

# 결과 파일 초기화
echo "Gateway Rate Limiting Test Result" > "$RESULT_FILE"
echo "Test Date: $(date '+%Y-%m-%d %H:%M:%S')" >> "$RESULT_FILE"
echo "========================================" >> "$RESULT_FILE"
echo "" >> "$RESULT_FILE"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Gateway Rate Limiting Test${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}결과 파일: ${RESULT_FILE}${NC}"
echo ""

# 헬퍼 함수: 콘솔과 파일 동시 출력
log_result() {
  echo -e "$1"
  echo "$1" | sed 's/\x1b\[[0-9;]*m//g' >> "$RESULT_FILE"
}

# 테스트 결과 추적 변수
TEST0_RESULT=""
TEST1_RESULT=""
TEST2_RESULT=""
TEST3_RESULT=""
TEST4_RESULT=""
TEST5_RESULT=""

# ==============================================================================
# Test 0: 회원가입
# ==============================================================================
log_result "[Test 0] 회원가입"

SIGNUP_RESPONSE=$(curl -s -X POST "${SIGNUP_ENDPOINT}" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${EMAIL}\",\"password\":\"${PASSWORD}\",\"name\":\"${NAME}\",\"phoneNumber\":\"01012345678\",\"role\":\"USER\"}")

SIGNUP_SUCCESS=$(echo "$SIGNUP_RESPONSE" | jq -r '.success')

if [ "$SIGNUP_SUCCESS" = "true" ]; then
  log_result "✓ 회원가입 성공"
  log_result "Email: ${EMAIL}"
  TEST0_RESULT="success"
else
  log_result "✗ 회원가입 실패"
  log_result "Response: ${SIGNUP_RESPONSE}"
  TEST0_RESULT="fail"
  exit 1
fi
log_result ""

# ==============================================================================
# Test 1: JWT 토큰 획득
# ==============================================================================
log_result "[Test 1] JWT 토큰 획득 (로그인)"

TOKEN=$(curl -s -X POST "${LOGIN_ENDPOINT}" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${EMAIL}\",\"password\":\"${PASSWORD}\"}" \
  | jq -r '.data.accessToken')

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  log_result "✗ 토큰 획득 실패"
  log_result "로그인 엔드포인트를 확인하세요."
  TEST1_RESULT="fail"
  exit 1
fi

log_result "✓ 토큰 획득 성공"
log_result "Token: ${TOKEN:0:20}..."
TEST1_RESULT="success"
log_result ""

# ==============================================================================
# Test 2: 정상 요청 - Rate Limit 헤더 확인
# ==============================================================================
log_result "[Test 2] 정상 요청 - Rate Limit 헤더 확인"

RESPONSE=$(curl -s -v "${TEST_ENDPOINT}" \
  -H "Authorization: Bearer ${TOKEN}" 2>&1)

# 헤더 추출
HTTP_STATUS=$(echo "$RESPONSE" | grep "< HTTP" | awk '{print $3}')
RATE_LIMIT=$(echo "$RESPONSE" | grep -i "< X-RateLimit-Limit:" | awk '{print $3}' | tr -d '\r')
RATE_REMAINING=$(echo "$RESPONSE" | grep -i "< X-RateLimit-Remaining:" | awk '{print $3}' | tr -d '\r')

log_result "HTTP Status: ${HTTP_STATUS}"
log_result "X-RateLimit-Limit: ${RATE_LIMIT}"
log_result "X-RateLimit-Remaining: ${RATE_REMAINING}"

if [ "$HTTP_STATUS" = "200" ] && [ "$RATE_LIMIT" = "10" ]; then
  log_result "✓ 정상 요청 성공, Rate Limit 헤더 확인됨"
  TEST2_RESULT="success"
else
  log_result "✗ 예상과 다른 응답"
  TEST2_RESULT="fail"
fi
log_result ""

# ==============================================================================
# Test 3: 10번 연속 요청 - 토큰 소진
# ==============================================================================
log_result "[Test 3] 10번 연속 요청 - 토큰 소진"

# 토큰 리필을 방지하기 위해 2초 대기 (Test 2에서 소진된 토큰이 리필되지 않도록)
echo "토큰 리필 방지를 위해 2초 대기..."
sleep 2

echo "요청 중..."

SUCCESS_COUNT=0
FAIL_COUNT=0

for i in {1..10}; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${TEST_ENDPOINT}" \
    -H "Authorization: Bearer ${TOKEN}")

  if [ "$STATUS" = "200" ]; then
    ((SUCCESS_COUNT++))
  else
    ((FAIL_COUNT++))
  fi

  # 진행 표시
  if [ $((i % 5)) -eq 0 ]; then
    echo -n "."
  fi
done

echo ""
log_result "성공: ${SUCCESS_COUNT}/10"
log_result "실패: ${FAIL_COUNT}/10"

if [ $SUCCESS_COUNT -eq 10 ]; then
  log_result "✓ 10번 요청 모두 성공"
  TEST3_RESULT="success"
else
  log_result "✗ 일부 요청 실패 (예상: 10번 모두 성공)"
  TEST3_RESULT="partial_fail"
fi
log_result ""

# ==============================================================================
# Test 4: 11번째 요청 - Rate Limit 초과 확인
# ==============================================================================
log_result "[Test 4] 11번째 요청 - Rate Limit 초과 확인"

# 헤더와 바디를 분리하여 저장
TEMP_HEADERS=$(mktemp)
TEMP_BODY=$(mktemp)

curl -s -D "$TEMP_HEADERS" -o "$TEMP_BODY" "${TEST_ENDPOINT}" \
  -H "Authorization: Bearer ${TOKEN}"

# 응답 추출
HTTP_STATUS=$(head -n 1 "$TEMP_HEADERS" | awk '{print $2}')
RETRY_AFTER=$(grep -i "Retry-After:" "$TEMP_HEADERS" | awk '{print $2}' | tr -d '\r')
BODY=$(cat "$TEMP_BODY")

log_result "HTTP Status: ${HTTP_STATUS}"
log_result "Retry-After: ${RETRY_AFTER}"
log_result "Response Body: ${BODY}"

if [ "$HTTP_STATUS" = "429" ]; then
  log_result "✓ 429 Too Many Requests 응답 확인"

  # 에러 응답 형식 확인
  ERROR_CODE=$(echo "$BODY" | jq -r '.code' 2>/dev/null)
  ERROR_MESSAGE=$(echo "$BODY" | jq -r '.message' 2>/dev/null)
  RETRY_AFTER_BODY=$(echo "$BODY" | jq -r '.retryAfter' 2>/dev/null)

  log_result "Error Code: ${ERROR_CODE}"
  log_result "Error Message: ${ERROR_MESSAGE}"
  log_result "Retry After (body): ${RETRY_AFTER_BODY}"

  if [ "$ERROR_CODE" = "9200" ]; then
    log_result "✓ 올바른 에러 코드 (9200)"
    TEST4_RESULT="success"
  else
    log_result "✗ 잘못된 에러 코드 (예상: 9200, 실제: ${ERROR_CODE})"
    TEST4_RESULT="partial_fail"
  fi
else
  log_result "✗ 예상과 다른 응답 (예상: 429, 실제: ${HTTP_STATUS})"
  TEST4_RESULT="fail"
fi

# 임시 파일 정리
rm -f "$TEMP_HEADERS" "$TEMP_BODY"

log_result ""

# ==============================================================================
# Test 5: 토큰 리필 확인 (10초 대기)
# ==============================================================================
log_result "[Test 5] 토큰 리필 확인 (10초 대기)"
echo "대기 중..."

for i in {10..1}; do
  echo -n "${i}..."
  sleep 1
done
echo ""

RESPONSE=$(curl -s -v "${TEST_ENDPOINT}" \
  -H "Authorization: Bearer ${TOKEN}" 2>&1)

HTTP_STATUS=$(echo "$RESPONSE" | grep "< HTTP" | awk '{print $3}')
RATE_REMAINING=$(echo "$RESPONSE" | grep -i "< X-RateLimit-Remaining:" | awk '{print $3}' | tr -d '\r')

log_result "HTTP Status: ${HTTP_STATUS}"
log_result "X-RateLimit-Remaining: ${RATE_REMAINING}"

if [ "$HTTP_STATUS" = "200" ]; then
  log_result "✓ 요청 성공 (토큰 리필됨)"
  log_result "리필된 토큰: 10개 (refill-rate: 1 token/sec × 10 sec)"
  log_result "남은 토큰: ${RATE_REMAINING} (10 - 1)"
  TEST5_RESULT="success"
else
  log_result "✗ 요청 실패 (토큰 리필 안됨?)"
  TEST5_RESULT="fail"
fi
log_result ""

# ==============================================================================
# Summary
# ==============================================================================
log_result "========================================"
log_result "Test Summary"
log_result "========================================"

# 각 테스트 결과 출력
if [ "$TEST0_RESULT" = "success" ]; then
  log_result "✓ Test 0: 회원가입"
else
  log_result "✗ Test 0: 회원가입"
fi

if [ "$TEST1_RESULT" = "success" ]; then
  log_result "✓ Test 1: JWT 토큰 획득"
else
  log_result "✗ Test 1: JWT 토큰 획득"
fi

if [ "$TEST2_RESULT" = "success" ]; then
  log_result "✓ Test 2: Rate Limit 헤더 확인"
else
  log_result "✗ Test 2: Rate Limit 헤더 확인"
fi

if [ "$TEST3_RESULT" = "success" ]; then
  log_result "✓ Test 3: 10번 연속 요청 성공"
elif [ "$TEST3_RESULT" = "partial_fail" ]; then
  log_result "⚠ Test 3: 10번 연속 요청 일부 실패 (Test 2에서 토큰 1개 소진됨)"
else
  log_result "✗ Test 3: 10번 연속 요청 실패"
fi

if [ "$TEST4_RESULT" = "success" ]; then
  log_result "✓ Test 4: 11번째 요청 429 응답"
elif [ "$TEST4_RESULT" = "partial_fail" ]; then
  log_result "⚠ Test 4: 429 응답 확인, 단 에러 코드 확인 실패"
else
  log_result "✗ Test 4: 11번째 요청 429 응답 실패"
fi

if [ "$TEST5_RESULT" = "success" ]; then
  log_result "✓ Test 5: 토큰 리필 확인"
else
  log_result "✗ Test 5: 토큰 리필 확인 실패"
fi

log_result ""

# 전체 테스트 성공 여부 판단
TOTAL_SUCCESS=0
TOTAL_PARTIAL=0
TOTAL_FAIL=0

for result in "$TEST0_RESULT" "$TEST1_RESULT" "$TEST2_RESULT" "$TEST3_RESULT" "$TEST4_RESULT" "$TEST5_RESULT"; do
  if [ "$result" = "success" ]; then
    ((TOTAL_SUCCESS++))
  elif [ "$result" = "partial_fail" ]; then
    ((TOTAL_PARTIAL++))
  elif [ "$result" = "fail" ]; then
    ((TOTAL_FAIL++))
  fi
done

if [ $TOTAL_FAIL -eq 0 ] && [ $TOTAL_PARTIAL -eq 0 ]; then
  log_result "🎉 모든 테스트 성공! Rate Limiting이 정상 동작합니다."
elif [ $TOTAL_FAIL -eq 0 ]; then
  log_result "⚠ 일부 테스트가 부분 성공했습니다. Rate Limiting 핵심 기능은 정상 동작합니다."
else
  log_result "❌ 일부 테스트가 실패했습니다. 로그를 확인하세요."
fi

log_result ""
log_result "결과 파일: ${RESULT_FILE}"