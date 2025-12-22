#!/bin/bash

# ==============================================================================
# Gateway Timeout & Circuit Breaker 테스트 스크립트
# ==============================================================================
# 목적: Timeout 설정 및 Circuit Breaker 패턴 동작 검증
# 테스트 항목:
#   1. Circuit Breaker 상태 조회
#   2. User Service Circuit Breaker (Coupon Service 호출)
#   3. Gateway Circuit Breaker (User Service 다운)
#   4. ConnectException 처리
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
ME_ENDPOINT="${GATEWAY_URL}/api/v1/users/me"
COUPON_ENDPOINT="${GATEWAY_URL}/api/v1/users/me/coupons"
CB_STATUS_ENDPOINT="${GATEWAY_URL}/internal/actuator/circuitbreakers"
CB_EVENTS_ENDPOINT="${GATEWAY_URL}/internal/actuator/circuitbreakerevents"

# 테스트 사용자 credentials (매번 랜덤 이메일로 새 계정 생성)
TIMESTAMP=$(date +%s)
EMAIL="test_${TIMESTAMP}@example.com"
PASSWORD="password1234@"
NAME="TestUser${TIMESTAMP}"

# 결과 파일 경로 (scripts 폴더 내)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESULT_FILE="${SCRIPT_DIR}/timeout-circuitbreaker-test-result-${TIMESTAMP}.txt"

# 결과 파일 초기화
echo "Gateway Timeout & Circuit Breaker Test Result" > "$RESULT_FILE"
echo "Test Date: $(date '+%Y-%m-%d %H:%M:%S')" >> "$RESULT_FILE"
echo "========================================" >> "$RESULT_FILE"
echo "" >> "$RESULT_FILE"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Gateway Timeout & Circuit Breaker Test${NC}"
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
  TEST1_RESULT="fail"
  exit 1
fi

log_result "✓ 토큰 획득 성공"
log_result "Token: ${TOKEN:0:20}..."
TEST1_RESULT="success"
log_result ""

# ==============================================================================
# Test 2: Circuit Breaker 상태 조회
# ==============================================================================
log_result "[Test 2] Circuit Breaker 상태 조회"
log_result "Endpoint: ${CB_STATUS_ENDPOINT}"

CB_STATUS=$(curl -s "${CB_STATUS_ENDPOINT}")

if [ $? -ne 0 ] || [ -z "$CB_STATUS" ]; then
  log_result "✗ Circuit Breaker 상태 조회 실패"
  log_result "Actuator 엔드포인트 설정을 확인하세요."
  TEST2_RESULT="fail"
else
  log_result "Response:"
  log_result "$(echo "$CB_STATUS" | jq '.')"

  # userServiceCircuitBreaker 확인
  CB_STATE=$(echo "$CB_STATUS" | jq -r '.circuitBreakers.userServiceCircuitBreaker.state' 2>/dev/null)

  if [ -n "$CB_STATE" ] && [ "$CB_STATE" != "null" ]; then
    log_result "✓ Circuit Breaker 상태 확인됨"
    log_result "State: $CB_STATE"
    TEST2_RESULT="success"
  else
    log_result "⚠ userServiceCircuitBreaker 없음"
    TEST2_RESULT="partial_fail"
  fi
fi

log_result ""

# ==============================================================================
# Test 3: User Service Circuit Breaker (Coupon Service 호출)
# ==============================================================================
log_result "[Test 3] User Service Circuit Breaker (Coupon Service 호출)"
log_result ""

log_result "1단계: Coupon Service 중지"
log_result "명령어: docker stop msa-coupon"
read -p "Coupon Service를 중지했습니까? (y/n): " coupon_stopped

if [ "$coupon_stopped" != "y" ]; then
  log_result "⚠ 테스트를 건너뜁니다."
  TEST3_RESULT="skipped"
  log_result ""
else
  log_result ""
  log_result "2단계: 내 쿠폰 조회 (5회 반복)"

  SUCCESS_COUNT=0
  FALLBACK_COUNT=0

  for i in {1..5}; do
    # 헤더와 바디 분리 (curl -i 옵션 사용)
    # -i: Include protocol response headers in the output
    FULL_RESPONSE=$(curl -s -i \
      -H "Authorization: Bearer ${TOKEN}" \
      "${COUPON_ENDPOINT}")

    # HTTP Status Code 추출 (마지막 라인 근처의 HTTP/1.1 200 OK 등 파싱은 복잡하므로 기존 방식 유지하되, -w 사용 불가 시 -i 출력 파싱)
    # 여기서는 간단히 curl -i 결과에서 헤더 확인. HTTP Code는 -w로 별도 확인이 깔끔하나, 한번 요청에 다 하려면 -i가 낫음.
    # 하지만 기존 스크립트 구조 유지를 위해, 요청을 수정하기보다 -D (dump-header) 사용 권장.
    
    # 2단계: 내 쿠폰 조회 (5회 반복) - 재구현
    # 헤더 파일 임시 저장
    HEADER_FILE="/tmp/headers.$$.txt"
    RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
      -D "$HEADER_FILE" \
      -H "Authorization: Bearer ${TOKEN}" \
      "${COUPON_ENDPOINT}")

    HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE" | cut -d':' -f2)
    BODY=$(echo "$RESPONSE" | sed '/HTTP_CODE/d')

    log_result "요청 #${i}: HTTP ${HTTP_CODE}"

    if [ "$HTTP_CODE" = "200" ]; then
      ((SUCCESS_COUNT++))

      # Fallback 헤더 확인
      FALLBACK_TRIGGERED=$(grep "X-Fallback-Triggered" "$HEADER_FILE")

      if [ -n "$FALLBACK_TRIGGERED" ]; then
        ((FALLBACK_COUNT++))
        log_result "  → Fallback 동작 (${FALLBACK_TRIGGERED})"
      else
        DATA=$(echo "$BODY" | jq -r '.data')
        log_result "  → 정상 응답 (보유 쿠폰: $(echo "$DATA" | jq 'length')개)"
      fi
      
      rm -f "$HEADER_FILE"
    fi

    sleep 2
  done

  log_result ""
  log_result "결과: 성공 ${SUCCESS_COUNT}/5, Fallback ${FALLBACK_COUNT}/5"

  if [ $FALLBACK_COUNT -ge 3 ]; then
    log_result "✓ User Service Circuit Breaker Fallback 동작 확인"
    TEST3_RESULT="success"
  else
    log_result "⚠ Fallback 동작 미확인"
    TEST3_RESULT="partial_fail"
  fi

  log_result ""
  log_result "3단계: Coupon Service 재시작"
  log_result "명령어: docker start msa-coupon"
  read -p "Coupon Service를 재시작했습니까? (y/n): " coupon_started
  log_result ""
fi

# ==============================================================================
# Test 4: Gateway Circuit Breaker (User Service 다운)
# ==============================================================================
log_result "[Test 4] Gateway Circuit Breaker (User Service 다운)"
log_result ""

log_result "1단계: User Service 중지"
log_result "명령어: docker stop msa-user"
read -p "User Service를 중지했습니까? (y/n): " user_stopped

if [ "$user_stopped" != "y" ]; then
  log_result "⚠ 테스트를 건너뜁니다."
  TEST4_RESULT="skipped"
  log_result ""
else
  log_result ""
  log_result "2단계: Gateway에서 User Service 호출 (10회 반복)"

  TIMEOUT_COUNT=0
  FALLBACK_COUNT=0

  for i in {1..10}; do
    RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
      -m 10 \
      "${ME_ENDPOINT}" \
      -H "Authorization: Bearer ${TOKEN}" 2>&1)

    HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE" | cut -d':' -f2)

    if [ -z "$HTTP_CODE" ]; then
      HTTP_CODE="TIMEOUT"
    fi

    log_result "요청 #${i}: HTTP ${HTTP_CODE}"

    if [ "$HTTP_CODE" = "503" ]; then
      BODY=$(echo "$RESPONSE" | sed '/HTTP_CODE/d')
      ERROR_CODE=$(echo "$BODY" | jq -r '.code' 2>/dev/null)

      if [ "$ERROR_CODE" = "9300" ]; then
        ((FALLBACK_COUNT++))
        log_result "  → Circuit Breaker Fallback (9300)"
      else
        ((TIMEOUT_COUNT++))
        log_result "  → Connect Timeout (9503)"
      fi
    elif [ "$HTTP_CODE" = "TIMEOUT" ]; then
      ((TIMEOUT_COUNT++))
      log_result "  → Connection Timeout"
    fi

    sleep 1
  done

  log_result ""
  log_result "결과: Timeout ${TIMEOUT_COUNT}/10, Fallback ${FALLBACK_COUNT}/10"

  if [ $((TIMEOUT_COUNT + FALLBACK_COUNT)) -ge 8 ]; then
    log_result "✓ Gateway Circuit Breaker 동작 확인"
    TEST4_RESULT="success"
  else
    log_result "⚠ Circuit Breaker 동작 미확인"
    TEST4_RESULT="partial_fail"
  fi

  log_result ""
  log_result "3단계: Circuit Breaker 상태 확인 (OPEN 예상)"
  CB_STATE=$(curl -s "${CB_STATUS_ENDPOINT}" | jq -r '.circuitBreakers.userServiceCircuitBreaker.state')
  log_result "State: $CB_STATE"

  log_result ""
  log_result "4단계: User Service 재시작"
  log_result "명령어: docker start msa-user"
  read -p "User Service를 재시작했습니까? (y/n): " user_started

  if [ "$user_started" = "y" ]; then
    log_result ""
    log_result "5단계: 서비스 준비 대기 (30초)"
    for i in {30..1}; do
      echo -ne "\r남은 시간: ${i}초  "
      sleep 1
    done
    echo ""

    log_result ""
    log_result "6단계: 30초 대기 (Half-Open 전환 대기)"
    for i in {30..1}; do
      echo -ne "\r남은 시간: ${i}초  "
      sleep 1
    done
    echo ""

    log_result ""
    log_result "7단계: Half-Open 상태 확인"
    CB_STATE=$(curl -s "${CB_STATUS_ENDPOINT}" | jq -r '.circuitBreakers.userServiceCircuitBreaker.state')
    log_result "State: $CB_STATE"

    log_result ""
    log_result "8단계: 복구 요청 3회 (CLOSED 전환 유도)"

    for i in {1..3}; do
      HTTP_CODE=$(curl -s -w "%{http_code}" -o /dev/null \
        -H "Authorization: Bearer ${TOKEN}" \
        "${ME_ENDPOINT}")

      log_result "요청 #${i}: HTTP ${HTTP_CODE}"
      sleep 2
    done

    log_result ""
    log_result "9단계: Circuit CLOSED 확인"
    CB_STATE=$(curl -s "${CB_STATUS_ENDPOINT}" | jq -r '.circuitBreakers.userServiceCircuitBreaker.state')
    log_result "State: $CB_STATE"

    if [ "$CB_STATE" = "CLOSED" ]; then
      log_result "✓ Circuit 복구 완료 (CLOSED)"
    else
      log_result "⚠ Circuit 상태: $CB_STATE (복구 중)"
    fi
  fi

  log_result ""
fi

# ==============================================================================
# Test 5: ConnectException 처리 개선 확인
# ==============================================================================
log_result "[Test 5] ConnectException 처리 개선 확인"
log_result ""

log_result "1단계: Product Service 중지"
log_result "명령어: docker stop msa-product"
read -p "Product Service를 중지했습니까? (y/n): " product_stopped

if [ "$product_stopped" != "y" ]; then
  log_result "⚠ 테스트를 건너뜁니다."
  TEST5_RESULT="skipped"
  log_result ""
else
  log_result ""
  log_result "2단계: Product Service 호출 (Circuit Breaker 없음)"

  RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" \
    -m 10 \
    -H "Authorization: Bearer ${TOKEN}" \
    "${GATEWAY_URL}/api/v1/products/test" 2>&1)

  HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE" | cut -d':' -f2)

  if [ -z "$HTTP_CODE" ]; then
    HTTP_CODE="TIMEOUT"
  fi

  BODY=$(echo "$RESPONSE" | sed '/HTTP_CODE/d')
  ERROR_CODE=$(echo "$BODY" | jq -r '.code' 2>/dev/null)

  log_result "HTTP Code: ${HTTP_CODE}"
  log_result "Error Code: ${ERROR_CODE}"

  if [ "$HTTP_CODE" = "503" ] && [ "$ERROR_CODE" = "9503" ]; then
    log_result "✓ ConnectException 정상 처리 (503, 9503)"
    TEST5_RESULT="success"
  elif [ "$HTTP_CODE" = "503" ]; then
    log_result "⚠ 503 응답 확인, Error Code 미일치"
    TEST5_RESULT="partial_fail"
  else
    log_result "✗ 예상과 다른 응답 (예상: 503/9503, 실제: ${HTTP_CODE}/${ERROR_CODE})"
    TEST5_RESULT="fail"
  fi

  log_result ""
  log_result "3단계: Product Service 재시작"
  log_result "명령어: docker start msa-product"
  read -p "Product Service를 재시작했습니까? (y/n): " product_started
  log_result ""
fi

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
  log_result "✓ Test 2: Circuit Breaker 상태 조회"
elif [ "$TEST2_RESULT" = "partial_fail" ]; then
  log_result "⚠ Test 2: Circuit Breaker 상태 조회 (일부 확인)"
else
  log_result "✗ Test 2: Circuit Breaker 상태 조회"
fi

if [ "$TEST3_RESULT" = "success" ]; then
  log_result "✓ Test 3: User Service Circuit Breaker"
elif [ "$TEST3_RESULT" = "skipped" ]; then
  log_result "⊝ Test 3: User Service Circuit Breaker (건너뜀)"
elif [ "$TEST3_RESULT" = "partial_fail" ]; then
  log_result "⚠ Test 3: User Service Circuit Breaker (일부 확인)"
else
  log_result "✗ Test 3: User Service Circuit Breaker"
fi

if [ "$TEST4_RESULT" = "success" ]; then
  log_result "✓ Test 4: Gateway Circuit Breaker"
elif [ "$TEST4_RESULT" = "skipped" ]; then
  log_result "⊝ Test 4: Gateway Circuit Breaker (건너뜀)"
elif [ "$TEST4_RESULT" = "partial_fail" ]; then
  log_result "⚠ Test 4: Gateway Circuit Breaker (일부 확인)"
else
  log_result "✗ Test 4: Gateway Circuit Breaker"
fi

if [ "$TEST5_RESULT" = "success" ]; then
  log_result "✓ Test 5: ConnectException 처리"
elif [ "$TEST5_RESULT" = "partial_fail" ]; then
  log_result "⚠ Test 5: ConnectException 처리 (일부 확인)"
else
  log_result "✗ Test 5: ConnectException 처리"
fi

log_result ""

# 전체 테스트 성공 여부 판단
TOTAL_SUCCESS=0
TOTAL_PARTIAL=0
TOTAL_FAIL=0
TOTAL_SKIPPED=0

for result in "$TEST0_RESULT" "$TEST1_RESULT" "$TEST2_RESULT" "$TEST3_RESULT" "$TEST4_RESULT" "$TEST5_RESULT"; do
  if [ "$result" = "success" ]; then
    ((TOTAL_SUCCESS++))
  elif [ "$result" = "partial_fail" ]; then
    ((TOTAL_PARTIAL++))
  elif [ "$result" = "fail" ]; then
    ((TOTAL_FAIL++))
  elif [ "$result" = "skipped" ]; then
    ((TOTAL_SKIPPED++))
  fi
done

if [ $TOTAL_FAIL -eq 0 ] && [ $TOTAL_PARTIAL -eq 0 ]; then
  log_result "🎉 모든 테스트 성공 Timeout & Circuit Breaker가 정상 동작합니다."
elif [ $TOTAL_FAIL -eq 0 ]; then
  log_result "⚠ 일부 테스트가 부분 성공했습니다. 핵심 기능은 정상 동작합니다."
else
  log_result "❌ 일부 테스트가 실패했습니다. 로그를 확인하세요."
fi

if [ $TOTAL_SKIPPED -gt 0 ]; then
  log_result "ℹ ${TOTAL_SKIPPED}개 테스트를 건너뛰었습니다."
fi

log_result ""
log_result "결과 파일: ${RESULT_FILE}"
log_result ""
log_result "추가 확인사항:"
log_result "  - Gateway 로그: docker-compose -f docker-compose.dev.yml logs -f gateway-service"
log_result "  - User Service 로그: docker-compose -f docker-compose.dev.yml logs -f user-service"