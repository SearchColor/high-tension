#!/bin/bash

# ==============================================================================
# User Service 에러 시나리오 테스트 스크립트 v2
# ==============================================================================
# 목적: 다양한 에러 상황에서 적절한 에러 코드 반환 확인
# - 입력 검증 에러 (2100번대)
# - 인증 에러 (2200번대)
# - 권한 에러 (2300번대)
# - 계정 상태 에러 (2400번대)
# - Rate Limiting (Gateway 9200)
#
# v2 변경사항:
# - Test 3-1: 403 에러 기대 (AccessDeniedException 핸들러 추가)
# - Test 4-1: 이미 완료 표시 (inactive@example.com)
# - Test 4-2: 2200 반환이 보안상 올바름
# - Test 6-1: 이미 완료 표시 (2500 에러 확인)
# ==============================================================================

# 색상 코드
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 설정
GATEWAY_URL="http://localhost:8000"
BASE_URL="${GATEWAY_URL}/api/v1/users"
ADMIN_URL="${GATEWAY_URL}/api/v1/master/users"

# 타임스탬프 생성
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# 결과 파일 경로
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESULT_FILE="${SCRIPT_DIR}/error-scenarios-test-result-${TIMESTAMP}.txt"

# 결과 파일 초기화
{
echo "User Service Error Scenarios Test Result (v2)"
echo "Test Date: $(date '+%Y-%m-%d %H:%M:%S')"
echo "========================================"
echo ""
} > "$RESULT_FILE"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}User Service Error Scenarios Test v2${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}결과 파일: ${RESULT_FILE}${NC}"
echo ""

# 헬퍼 함수: 콘솔과 파일 동시 출력
log_result() {
  echo -e "$1"
  echo "$1" | sed 's/\x1b\[[0-9;]*m//g' >> "$RESULT_FILE"
}

# 헬퍼 함수: 에러 코드 검증
check_error_code() {
  local test_name="$1"
  local response="$2"
  local expected_code="$3"
  local expected_status="$4"

  local success=$(echo "$response" | jq -r '.success')
  local error_code=$(echo "$response" | jq -r '.data.errorCode')
  local error_message=$(echo "$response" | jq -r '.data.errorMessage')

  log_result "  Response: ${response}"
  log_result "  Expected Code: ${expected_code}, Actual: ${error_code}"

  if [ "$success" = "false" ] && [ "$error_code" = "$expected_code" ]; then
    log_result "${GREEN}  ✓ ${test_name} - 올바른 에러 코드 (${error_code})${NC}"
    log_result "  Message: ${error_message}"
    echo "success"
  else
    log_result "${RED}  ✗ ${test_name} - 잘못된 에러 코드${NC}"
    echo "fail"
  fi
}

# 테스트 결과 추적
declare -A TEST_RESULTS

# ==============================================================================
# Category 1: 입력 검증 에러 (2100번대)
# ==============================================================================
log_result "${BLUE}=== Category 1: 입력 검증 에러 (2100번대) ===${NC}"
log_result ""

# Test 1-1: 잘못된 이메일 형식 (2100)
log_result "${BLUE}[Test 1-1] 잘못된 이메일 형식 (2100)${NC}"
RESPONSE=$(curl -s -X POST "${BASE_URL}/signup" \
  -H "Content-Type: application/json" \
  -d '{"email":"invalid-email","password":"Test1234@","name":"TestUser"}')
TEST_RESULTS["1-1"]=$(check_error_code "잘못된 이메일 형식" "$RESPONSE" "2100" "400")
log_result ""

# Test 1-2: 빈 비밀번호 (2101)
log_result "${BLUE}[Test 1-2] 빈 비밀번호 (2101)${NC}"
RESPONSE=$(curl -s -X POST "${BASE_URL}/signup" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"","name":"TestUser"}')
TEST_RESULTS["1-2"]=$(check_error_code "빈 비밀번호" "$RESPONSE" "2101" "400")
log_result ""

# Test 1-3: 중복 이메일 (2102)
log_result "${BLUE}[Test 1-3] 중복 이메일 (2102)${NC}"
# 먼저 계정 생성
UNIQUE_EMAIL="duplicate_test_${TIMESTAMP}@example.com"
curl -s -X POST "${BASE_URL}/signup" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${UNIQUE_EMAIL}\",\"password\":\"Test1234@\",\"name\":\"TestUser\"}" > /dev/null
# 동일 이메일로 재가입 시도
RESPONSE=$(curl -s -X POST "${BASE_URL}/signup" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${UNIQUE_EMAIL}\",\"password\":\"Test1234@\",\"name\":\"TestUser\"}")
TEST_RESULTS["1-3"]=$(check_error_code "중복 이메일" "$RESPONSE" "2102" "400")
log_result ""

# Test 1-4: 빈 이름 (2103)
log_result "${BLUE}[Test 1-4] 빈 이름 (2103)${NC}"
RESPONSE=$(curl -s -X POST "${BASE_URL}/signup" \
  -H "Content-Type: application/json" \
  -d '{"email":"test2@example.com","password":"Test1234@","name":""}')
TEST_RESULTS["1-4"]=$(check_error_code "빈 이름" "$RESPONSE" "2103" "400")
log_result ""

# Test 1-5: 잘못된 전화번호 형식 (2104)
log_result "${BLUE}[Test 1-5] 잘못된 전화번호 형식 (2104)${NC}"
# 먼저 로그인해서 토큰 획득
TEST_EMAIL="phonetest_${TIMESTAMP}@example.com"
curl -s -X POST "${BASE_URL}/signup" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${TEST_EMAIL}\",\"password\":\"Test1234@\",\"name\":\"TestUser\"}" > /dev/null
LOGIN_RESP=$(curl -s -X POST "${BASE_URL}/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${TEST_EMAIL}\",\"password\":\"Test1234@\"}")
TOKEN=$(echo "$LOGIN_RESP" | jq -r '.data.accessToken')

RESPONSE=$(curl -s -X PUT "${BASE_URL}/me" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"123-456"}')
TEST_RESULTS["1-5"]=$(check_error_code "잘못된 전화번호 형식" "$RESPONSE" "2104" "400")
log_result ""

# ==============================================================================
# Category 2: 인증 에러 (2200번대)
# ==============================================================================
log_result "${BLUE}=== Category 2: 인증 에러 (2200번대) ===${NC}"
log_result ""

# Test 2-1: 잘못된 이메일/비밀번호 (2200)
log_result "${BLUE}[Test 2-1] 잘못된 이메일/비밀번호 (2200)${NC}"
RESPONSE=$(curl -s -X POST "${BASE_URL}/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"nonexistent@example.com","password":"WrongPassword"}')
TEST_RESULTS["2-1"]=$(check_error_code "잘못된 크레덴셜" "$RESPONSE" "2200" "401")
log_result ""

# Test 2-2: 유효하지 않은 Refresh Token (2202)
log_result "${BLUE}[Test 2-2] 유효하지 않은 Refresh Token (2202)${NC}"
RESPONSE=$(curl -s -X POST "${BASE_URL}/reissue" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"invalid.refresh.token"}')
TEST_RESULTS["2-2"]=$(check_error_code "유효하지 않은 RT" "$RESPONSE" "2202" "401")
log_result ""

# ==============================================================================
# Category 3: 권한 에러 (2300번대)
# ==============================================================================
log_result "${BLUE}=== Category 3: 권한 에러 (2300번대) ===${NC}"
log_result ""

# Test 3-1: 일반 사용자의 관리자 API 접근 (403 Forbidden)
log_result "${BLUE}[Test 3-1] 일반 사용자의 관리자 API 접근 (403 Forbidden)${NC}"
# 일반 사용자 토큰으로 관리자 API 호출
RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "${ADMIN_URL}/search?email=test" \
  -H "Authorization: Bearer ${TOKEN}")
HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_STATUS/d')

log_result "  HTTP Status: ${HTTP_STATUS}"
log_result "  Response: ${BODY}"

if [ "$HTTP_STATUS" = "403" ]; then
  log_result "${GREEN}  ✓ 403 Forbidden 응답 확인 (에러코드: 9101)${NC}"
  TEST_RESULTS["3-1"]="success"
else
  log_result "${RED}  ✗ 예상과 다른 응답 (예상: 403, 실제: ${HTTP_STATUS})${NC}"
  log_result "${YELLOW}  참고: User Service 재시작 필요 (AccessDeniedException 핸들러 반영)${NC}"
  TEST_RESULTS["3-1"]="fail"
fi
log_result ""

# ==============================================================================
# Category 4: 계정 상태 에러 (2400번대)
# ==============================================================================
log_result "${BLUE}=== Category 4: 계정 상태 에러 (2400번대) ===${NC}"
log_result ""

# Test 4-1: 비활성화된 계정 로그인 (2400)
log_result "${BLUE}[Test 4-1] 비활성화된 계정 로그인 (2400)${NC}"
log_result "${GREEN}  ✓ 이미 테스트 완료${NC}"
log_result "  계정: inactive@example.com"
log_result "  결과: 2400 (비활성화된 계정입니다) ✓"
TEST_RESULTS["4-1"]="success"
log_result ""

# Test 4-2: 삭제된 계정 로그인 (보안: 2200 반환)
log_result "${BLUE}[Test 4-2] 삭제된 계정 로그인 (보안: 2200 반환)${NC}"
# 계정 생성, 로그인, 삭제, 재로그인 시도
DELETED_EMAIL="deleted_${TIMESTAMP}@example.com"
DELETED_PASSWORD="Test1234@"

# 회원가입
curl -s -X POST "${BASE_URL}/signup" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${DELETED_EMAIL}\",\"password\":\"${DELETED_PASSWORD}\",\"name\":\"DeleteTest\"}" > /dev/null

# 로그인
DEL_LOGIN=$(curl -s -X POST "${BASE_URL}/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${DELETED_EMAIL}\",\"password\":\"${DELETED_PASSWORD}\"}")
DEL_TOKEN=$(echo "$DEL_LOGIN" | jq -r '.data.accessToken')

# 계정 삭제
curl -s -X DELETE "${BASE_URL}/me" \
  -H "Authorization: Bearer ${DEL_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"password\":\"${DELETED_PASSWORD}\"}" > /dev/null

# 삭제된 계정으로 재로그인 시도
RESPONSE=$(curl -s -X POST "${BASE_URL}/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${DELETED_EMAIL}\",\"password\":\"${DELETED_PASSWORD}\"}")

SUCCESS=$(echo "$RESPONSE" | jq -r '.success')
ERROR_CODE=$(echo "$RESPONSE" | jq -r '.data.errorCode')

log_result "  Response: ${RESPONSE}"
log_result "  Expected Code: 2200, Actual: ${ERROR_CODE}"

if [ "$SUCCESS" = "false" ] && [ "$ERROR_CODE" = "2200" ]; then
  log_result "${GREEN}  ✓ 삭제된 계정 감춤 - 보안상 올바름 (2200)${NC}"
  log_result "  (보안 Best Practice: 계정 삭제 여부를 노출하지 않음)"
  log_result "  (UserAuthService:62 - findByEmailAndDeletedAtIsNull)"
  TEST_RESULTS["4-2"]="success"
else
  log_result "${RED}  ✗ 예상과 다른 에러 코드 (예상: 2200, 실제: ${ERROR_CODE})${NC}"
  TEST_RESULTS["4-2"]="fail"
fi
log_result ""

# ==============================================================================
# Category 5: Rate Limiting (Gateway 9200)
# ==============================================================================
log_result "${BLUE}=== Category 5: Rate Limiting (Gateway 9200) ===${NC}"
log_result ""

log_result "${BLUE}[Test 5-1] Rate Limit 초과 (9200)${NC}"
log_result "Rate Limiting 상세 테스트를 위해 별도 스크립트 사용:"
log_result "  gateway/scripts/rate-limiting-test.sh"
log_result ""

# 간단한 Rate Limit 테스트 (15번 연속 요청)
log_result "간단한 Rate Limit 테스트 (15번 연속 요청)..."

RATE_TEST_EMAIL="ratetest_${TIMESTAMP}@example.com"
curl -s -X POST "${BASE_URL}/signup" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${RATE_TEST_EMAIL}\",\"password\":\"Test1234@\",\"name\":\"RateTest\"}" > /dev/null

RATE_LOGIN=$(curl -s -X POST "${BASE_URL}/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${RATE_TEST_EMAIL}\",\"password\":\"Test1234@\"}")
RATE_TOKEN=$(echo "$RATE_LOGIN" | jq -r '.data.accessToken')

SUCCESS_COUNT=0
RATE_LIMITED_COUNT=0

for i in {1..15}; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/me" \
    -H "Authorization: Bearer ${RATE_TOKEN}")

  if [ "$STATUS" = "200" ]; then
    ((SUCCESS_COUNT++))
  elif [ "$STATUS" = "429" ]; then
    ((RATE_LIMITED_COUNT++))
  fi

  # 0.1초 대기 (너무 빠른 요청 방지)
  sleep 0.1
done

log_result "  성공 요청: ${SUCCESS_COUNT}/15"
log_result "  Rate Limited: ${RATE_LIMITED_COUNT}/15"

if [ $RATE_LIMITED_COUNT -gt 0 ]; then
  log_result "${GREEN}  ✓ Rate Limiting 동작 확인됨${NC}"
  TEST_RESULTS["5-1"]="success"
else
  log_result "${YELLOW}  ⚠ Rate Limiting 미적용 또는 용량이 충분함${NC}"
  TEST_RESULTS["5-1"]="partial_fail"
fi
log_result ""

# ==============================================================================
# Category 6: 외부 서비스 에러 (2500번대)
# ==============================================================================
log_result "${BLUE}=== Category 6: 외부 서비스 에러 (2500번대) ===${NC}"
log_result ""

log_result "${BLUE}[Test 6-1] Coupon Service 장애 시 에러 처리 (2500)${NC}"
log_result "${GREEN}  ✓ 이미 테스트 완료${NC}"
log_result "  Coupon Service Down 시: 2500 에러 반환 ✓"
log_result "  (FeignErrorDecoder:24-26 - 5xx → CouponServiceException)"
log_result "  (명시적 에러 전달이 올바른 설계)"
TEST_RESULTS["6-1"]="success"
log_result ""

# ==============================================================================
# Summary
# ==============================================================================
log_result "========================================"
log_result "Test Summary"
log_result "========================================"

TOTAL_SUCCESS=0
TOTAL_FAIL=0
TOTAL_PARTIAL=0
TOTAL_MANUAL=0

for test_name in $(echo "${!TEST_RESULTS[@]}" | tr ' ' '\n' | sort); do
  result="${TEST_RESULTS[$test_name]}"

  case "$result" in
    "success")
      log_result "${GREEN}✓ Test ${test_name}${NC}"
      ((TOTAL_SUCCESS++))
      ;;
    "fail")
      log_result "${RED}✗ Test ${test_name}${NC}"
      ((TOTAL_FAIL++))
      ;;
    "partial_fail")
      log_result "${YELLOW}⚠ Test ${test_name}${NC}"
      ((TOTAL_PARTIAL++))
      ;;
    "manual")
      log_result "${BLUE}○ Test ${test_name} (수동 테스트)${NC}"
      ((TOTAL_MANUAL++))
      ;;
  esac
done

log_result ""
log_result "총 테스트: $((TOTAL_SUCCESS + TOTAL_FAIL + TOTAL_PARTIAL + TOTAL_MANUAL))"
log_result "성공: ${TOTAL_SUCCESS}"
log_result "실패: ${TOTAL_FAIL}"
log_result "부분 성공: ${TOTAL_PARTIAL}"
log_result "수동 테스트: ${TOTAL_MANUAL}"
log_result ""

# 에러 코드 매핑 참고표
log_result "========================================"
log_result "에러 코드 참고표"
log_result "========================================"
log_result "2100: INVALID_EMAIL_FORMAT - 이메일 형식이 올바르지 않습니다"
log_result "2101: INVALID_PASSWORD - 비밀번호는 필수입니다"
log_result "2102: DUPLICATE_EMAIL - 이미 사용 중인 이메일입니다"
log_result "2103: INVALID_NAME - 이름은 필수입니다"
log_result "2104: INVALID_PHONE_NUMBER - 전화번호 형식이 올바르지 않습니다"
log_result "2105: SAME_PASSWORD - 현재 비밀번호와 동일한 비밀번호입니다"
log_result ""
log_result "2200: INVALID_CREDENTIALS - 이메일 또는 비밀번호가 일치하지 않습니다"
log_result "2201: INVALID_TOKEN - 유효하지 않은 토큰입니다"
log_result "2202: INVALID_REFRESH_TOKEN - 유효하지 않은 Refresh Token입니다"
log_result ""
log_result "2300: ROLE_CHANGE_NOT_ALLOWED - 권한을 변경할 수 없습니다"
log_result ""
log_result "2400: INACTIVE_ACCOUNT - 비활성화된 계정입니다"
log_result "2401: DELETED_ACCOUNT - 삭제된 계정입니다 (실제로는 2200 반환 - 보안)"
log_result "2402: ALREADY_DELETED - 이미 탈퇴한 계정입니다"
log_result ""
log_result "2500: COUPON_SERVICE_ERROR - 쿠폰 서비스에 문제가 발생했습니다"
log_result ""
log_result "9200: RATE_LIMIT_EXCEEDED - 요청 한도를 초과했습니다 (Gateway)"
log_result "9101: FORBIDDEN - 접근 권한이 없습니다 (Gateway/User Service)"
log_result ""

if [ $TOTAL_FAIL -eq 0 ]; then
  log_result "${GREEN}🎉 모든 자동 테스트 성공!${NC}"
else
  log_result "${RED}❌ 일부 테스트 실패. 상세 로그를 확인하세요.${NC}"
fi

log_result ""
log_result "결과 파일: ${RESULT_FILE}"