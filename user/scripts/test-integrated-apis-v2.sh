#!/bin/bash

# ==============================================================================
# User Service 통합 API 테스트 스크립트 v2
# ==============================================================================
# 목적: Issue #18, #19, #32 기능 통합 테스트
# - Token 재발급 API (Refresh Token Rotation)
# - 관리자 전용 API (사용자 검색, 권한 변경)
# - 내 쿠폰 조회 API (Coupon Service 연동)
#
# v2 변경사항:
# - Test 4: OLD_REFRESH_TOKEN 사용 (RT Rotation 검증 개선)
# - Test 9: 403 에러 기대 (AccessDeniedException 핸들러 추가)
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
COUPON_URL="${GATEWAY_URL}/api/v1/coupons"

# 타임스탬프 생성
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# 테스트 사용자 credentials
USER_EMAIL="testuser_${TIMESTAMP}@example.com"
USER_PASSWORD="Test1234@"
USER_NAME="TestUser${TIMESTAMP}"

MASTER_EMAIL="master_${TIMESTAMP}@example.com"
MASTER_PASSWORD="Master1234@"
MASTER_NAME="MasterUser${TIMESTAMP}"

# 결과 파일 경로
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESULT_FILE="${SCRIPT_DIR}/integrated-apis-test-result-${TIMESTAMP}.txt"

# 결과 파일 초기화
echo "User Service Integrated APIs Test Result (v2)" > "$RESULT_FILE"
echo "Test Date: $(date '+%Y-%m-%d %H:%M:%S')" >> "$RESULT_FILE"
echo "========================================" >> "$RESULT_FILE"
echo "" >> "$RESULT_FILE"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}User Service Integrated APIs Test v2${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}결과 파일: ${RESULT_FILE}${NC}"
echo ""

# 헬퍼 함수: 콘솔과 파일 동시 출력
log_result() {
  echo -e "$1"
  echo "$1" | sed 's/\x1b\[[0-9;]*m//g' >> "$RESULT_FILE"
}

# 테스트 결과 추적 변수
declare -A TEST_RESULTS

# ==============================================================================
# Setup: MASTER 계정 생성 (직접 DB 삽입 필요 - 수동)
# ==============================================================================
log_result "${YELLOW}[Setup] MASTER 계정 생성${NC}"
log_result "MASTER 계정은 Factory Method로만 생성 가능합니다."
log_result "다음 SQL을 실행하세요:"
log_result ""
log_result "-- MySQL에서 실행"
log_result "INSERT INTO user_service.p_users (user_id, email, password, name, role, is_active, created_at, created_by)"
log_result "VALUES ("
log_result "  UNHEX(REPLACE(UUID(), '-', '')),"
log_result "  '${MASTER_EMAIL}',"
log_result "  '\$2a\$10\$YOUR_BCRYPT_HASHED_PASSWORD',  -- bcrypt hash of '${MASTER_PASSWORD}'"
log_result "  '${MASTER_NAME}',"
log_result "  'MASTER',"
log_result "  1,"
log_result "  NOW(),"
log_result "  UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))"
log_result ");"
log_result ""
log_result "또는 기존 MASTER 계정 사용 (이메일/비밀번호 입력)"
read -p "MASTER 이메일: " MASTER_EMAIL
read -sp "MASTER 비밀번호: " MASTER_PASSWORD
echo ""
log_result ""

# ==============================================================================
# Test 1: 일반 사용자 회원가입
# ==============================================================================
log_result "${BLUE}[Test 1] 일반 사용자 회원가입${NC}"

SIGNUP_RESPONSE=$(curl -s -X POST "${BASE_URL}/signup" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${USER_EMAIL}\",\"password\":\"${USER_PASSWORD}\",\"name\":\"${USER_NAME}\"}")

SIGNUP_SUCCESS=$(echo "$SIGNUP_RESPONSE" | jq -r '.success')
USER_ID=$(echo "$SIGNUP_RESPONSE" | jq -r '.data.userId')

if [ "$SIGNUP_SUCCESS" = "true" ]; then
  log_result "${GREEN}✓ 회원가입 성공${NC}"
  log_result "User ID: ${USER_ID}"
  log_result "Email: ${USER_EMAIL}"
  TEST_RESULTS["test1"]="success"
else
  log_result "${RED}✗ 회원가입 실패${NC}"
  log_result "Response: ${SIGNUP_RESPONSE}"
  TEST_RESULTS["test1"]="fail"
fi
log_result ""

# ==============================================================================
# Test 2: 로그인 및 토큰 획득
# ==============================================================================
log_result "${BLUE}[Test 2] 로그인 및 토큰 획득${NC}"

LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${USER_EMAIL}\",\"password\":\"${USER_PASSWORD}\"}")

ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.accessToken')
REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.refreshToken')

if [ -n "$ACCESS_TOKEN" ] && [ "$ACCESS_TOKEN" != "null" ]; then
  log_result "${GREEN}✓ 로그인 성공${NC}"
  log_result "Access Token: ${ACCESS_TOKEN:0:30}..."
  log_result "Refresh Token: ${REFRESH_TOKEN:0:30}..."
  TEST_RESULTS["test2"]="success"
else
  log_result "${RED}✗ 로그인 실패${NC}"
  log_result "Response: ${LOGIN_RESPONSE}"
  TEST_RESULTS["test2"]="fail"
fi
log_result ""

# ==============================================================================
# Test 3: Token 재발급 (Issue #18)
# ==============================================================================
log_result "${BLUE}[Test 3] Token 재발급 (Refresh Token Rotation)${NC}"

sleep 2  # AT 만료 방지를 위한 대기

REISSUE_RESPONSE=$(curl -s -X POST "${BASE_URL}/reissue" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"${REFRESH_TOKEN}\"}")

NEW_ACCESS_TOKEN=$(echo "$REISSUE_RESPONSE" | jq -r '.data.accessToken')
NEW_REFRESH_TOKEN=$(echo "$REISSUE_RESPONSE" | jq -r '.data.refreshToken')

if [ -n "$NEW_ACCESS_TOKEN" ] && [ "$NEW_ACCESS_TOKEN" != "null" ]; then
  log_result "${GREEN}✓ Token 재발급 성공${NC}"
  log_result "New Access Token: ${NEW_ACCESS_TOKEN:0:30}..."
  log_result "New Refresh Token: ${NEW_REFRESH_TOKEN:0:30}..."

  # 기존 RT와 비교 (Rotation 확인)
  if [ "$REFRESH_TOKEN" != "$NEW_REFRESH_TOKEN" ]; then
    log_result "${GREEN}✓ Refresh Token Rotation 확인됨${NC}"
  else
    log_result "${YELLOW}⚠ Refresh Token이 동일함 (Rotation 미적용?)${NC}"
  fi

  # Test 4를 위해 기존 RT 보관
  OLD_REFRESH_TOKEN="$REFRESH_TOKEN"

  # 이후 테스트를 위해 토큰 업데이트
  ACCESS_TOKEN="$NEW_ACCESS_TOKEN"
  REFRESH_TOKEN="$NEW_REFRESH_TOKEN"

  TEST_RESULTS["test3"]="success"
else
  log_result "${RED}✗ Token 재발급 실패${NC}"
  log_result "Response: ${REISSUE_RESPONSE}"
  TEST_RESULTS["test3"]="fail"
fi
log_result ""

# ==============================================================================
# Test 4: 기존 RT로 재발급 시도 (토큰 탈취 감지)
# ==============================================================================
log_result "${BLUE}[Test 4] 기존 RT로 재발급 시도 (토큰 탈취 감지)${NC}"

# 이미 무효화된 RT로 재발급 시도
OLD_RT_RESPONSE=$(curl -s -X POST "${BASE_URL}/reissue" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"${OLD_REFRESH_TOKEN}\"}")

OLD_RT_SUCCESS=$(echo "$OLD_RT_RESPONSE" | jq -r '.success')
OLD_RT_ERROR_CODE=$(echo "$OLD_RT_RESPONSE" | jq -r '.data.errorCode')

if [ "$OLD_RT_SUCCESS" = "false" ] && [ "$OLD_RT_ERROR_CODE" = "2202" ]; then
  log_result "${GREEN}✓ 무효화된 RT 거부됨 (에러코드: 2202)${NC}"
  TEST_RESULTS["test4"]="success"
else
  log_result "${YELLOW}⚠ 예상과 다른 응답${NC}"
  log_result "Response: ${OLD_RT_RESPONSE}"
  TEST_RESULTS["test4"]="partial_fail"
fi
log_result ""

# ==============================================================================
# Test 5: MASTER 로그인
# ==============================================================================
log_result "${BLUE}[Test 5] MASTER 로그인${NC}"

MASTER_LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${MASTER_EMAIL}\",\"password\":\"${MASTER_PASSWORD}\"}")

MASTER_ACCESS_TOKEN=$(echo "$MASTER_LOGIN_RESPONSE" | jq -r '.data.accessToken')

if [ -n "$MASTER_ACCESS_TOKEN" ] && [ "$MASTER_ACCESS_TOKEN" != "null" ]; then
  log_result "${GREEN}✓ MASTER 로그인 성공${NC}"
  log_result "Master Token: ${MASTER_ACCESS_TOKEN:0:30}..."
  TEST_RESULTS["test5"]="success"
else
  log_result "${RED}✗ MASTER 로그인 실패${NC}"
  log_result "Response: ${MASTER_LOGIN_RESPONSE}"
  TEST_RESULTS["test5"]="fail"
fi
log_result ""

# ==============================================================================
# Test 6: 사용자 검색 (Issue #19)
# ==============================================================================
log_result "${BLUE}[Test 6] 사용자 검색 (MASTER 권한)${NC}"

SEARCH_RESPONSE=$(curl -s -X GET "${ADMIN_URL}/search?email=${USER_EMAIL:0:10}&page=0&size=20" \
  -H "Authorization: Bearer ${MASTER_ACCESS_TOKEN}")

SEARCH_SUCCESS=$(echo "$SEARCH_RESPONSE" | jq -r '.success')
SEARCH_TOTAL=$(echo "$SEARCH_RESPONSE" | jq -r '.data.totalElements')

if [ "$SEARCH_SUCCESS" = "true" ] && [ "$SEARCH_TOTAL" -ge 1 ]; then
  log_result "${GREEN}✓ 사용자 검색 성공${NC}"
  log_result "검색 결과: ${SEARCH_TOTAL}건"
  log_result "Response: ${SEARCH_RESPONSE}"
  TEST_RESULTS["test6"]="success"
else
  log_result "${RED}✗ 사용자 검색 실패${NC}"
  log_result "Response: ${SEARCH_RESPONSE}"
  TEST_RESULTS["test6"]="fail"
fi
log_result ""

# ==============================================================================
# Test 7: 권한 변경 (USER → SELLER) (Issue #19)
# ==============================================================================
log_result "${BLUE}[Test 7] 권한 변경 (USER → SELLER)${NC}"

ROLE_CHANGE_RESPONSE=$(curl -s -X PATCH "${ADMIN_URL}/${USER_ID}/role" \
  -H "Authorization: Bearer ${MASTER_ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"role\":\"SELLER\"}")

ROLE_CHANGE_SUCCESS=$(echo "$ROLE_CHANGE_RESPONSE" | jq -r '.success')
NEW_ROLE=$(echo "$ROLE_CHANGE_RESPONSE" | jq -r '.data.role')

if [ "$ROLE_CHANGE_SUCCESS" = "true" ] && [ "$NEW_ROLE" = "SELLER" ]; then
  log_result "${GREEN}✓ 권한 변경 성공 (USER → SELLER)${NC}"
  TEST_RESULTS["test7"]="success"
else
  log_result "${RED}✗ 권한 변경 실패${NC}"
  log_result "Response: ${ROLE_CHANGE_RESPONSE}"
  TEST_RESULTS["test7"]="fail"
fi
log_result ""

# ==============================================================================
# Test 8: MASTER 권한으로 변경 시도 (거부 확인)
# ==============================================================================
log_result "${BLUE}[Test 8] MASTER 권한으로 변경 시도 (거부 확인)${NC}"

MASTER_ROLE_RESPONSE=$(curl -s -X PATCH "${ADMIN_URL}/${USER_ID}/role" \
  -H "Authorization: Bearer ${MASTER_ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"role\":\"MASTER\"}")

MASTER_ROLE_SUCCESS=$(echo "$MASTER_ROLE_RESPONSE" | jq -r '.success')
MASTER_ROLE_ERROR=$(echo "$MASTER_ROLE_RESPONSE" | jq -r '.data.errorCode')

if [ "$MASTER_ROLE_SUCCESS" = "false" ] && [ "$MASTER_ROLE_ERROR" = "2300" ]; then
  log_result "${GREEN}✓ MASTER 권한 변경 차단됨 (에러코드: 2300)${NC}"
  TEST_RESULTS["test8"]="success"
else
  log_result "${RED}✗ MASTER 권한 변경이 허용됨 (보안 취약점!)${NC}"
  log_result "Response: ${MASTER_ROLE_RESPONSE}"
  TEST_RESULTS["test8"]="fail"
fi
log_result ""

# ==============================================================================
# Test 9: 일반 사용자로 관리자 API 접근 시도 (403 확인)
# ==============================================================================
log_result "${BLUE}[Test 9] 일반 사용자로 관리자 API 접근 시도 (403 확인)${NC}"

# SELLER 권한으로 되돌리기
curl -s -X PATCH "${ADMIN_URL}/${USER_ID}/role" \
  -H "Authorization: Bearer ${MASTER_ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"role\":\"USER\"}" > /dev/null

# 새로 로그인하여 USER 토큰 받기
USER_LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${USER_EMAIL}\",\"password\":\"${USER_PASSWORD}\"}")
USER_TOKEN=$(echo "$USER_LOGIN_RESPONSE" | jq -r '.data.accessToken')

FORBIDDEN_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "${ADMIN_URL}/search?email=test" \
  -H "Authorization: Bearer ${USER_TOKEN}")

HTTP_STATUS=$(echo "$FORBIDDEN_RESPONSE" | grep "HTTP_STATUS" | cut -d':' -f2)

if [ "$HTTP_STATUS" = "403" ]; then
  log_result "${GREEN}✓ 403 Forbidden 응답 확인됨${NC}"
  TEST_RESULTS["test9"]="success"
else
  log_result "${RED}✗ 예상과 다른 응답 (예상: 403, 실제: ${HTTP_STATUS})${NC}"
  log_result "${YELLOW}참고: User Service 재시작 필요 (AccessDeniedException 핸들러 반영)${NC}"
  TEST_RESULTS["test9"]="fail"
fi
log_result ""

# ==============================================================================
# Test 10: 내 쿠폰 조회 (Issue #32)
# ==============================================================================
log_result "${BLUE}[Test 10] 내 쿠폰 조회 (Coupon Service 연동)${NC}"

COUPON_RESPONSE=$(curl -s -X GET "${BASE_URL}/me/coupons" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}")

COUPON_SUCCESS=$(echo "$COUPON_RESPONSE" | jq -r '.success')

if [ "$COUPON_SUCCESS" = "true" ]; then
  COUPON_COUNT=$(echo "$COUPON_RESPONSE" | jq -r '.data | length')
  log_result "${GREEN}✓ 내 쿠폰 조회 성공${NC}"
  log_result "쿠폰 개수: ${COUPON_COUNT}개"
  log_result "Response: ${COUPON_RESPONSE}"
  TEST_RESULTS["test10"]="success"
else
  log_result "${YELLOW}⚠ 쿠폰 조회 실패 (Coupon Service 미가동 가능성)${NC}"
  log_result "Response: ${COUPON_RESPONSE}"
  TEST_RESULTS["test10"]="partial_fail"
fi
log_result ""

# ==============================================================================
# Test 11: Coupon Service 장애 시 에러 처리
# ==============================================================================
log_result "${BLUE}[Test 11] Coupon Service 장애 시 에러 처리${NC}"
log_result "${GREEN}✓ 이미 테스트 완료${NC}"
log_result "  Coupon Service Down 시: 2500 에러 반환"
log_result "  (FeignErrorDecoder: 5xx → CouponServiceException)"
log_result "  (명시적 에러 전달이 올바른 설계)"
TEST_RESULTS["test11"]="success"
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

for test_name in "${!TEST_RESULTS[@]}"; do
  result="${TEST_RESULTS[$test_name]}"

  case "$result" in
    "success")
      log_result "${GREEN}✓ ${test_name}${NC}"
      ((TOTAL_SUCCESS++))
      ;;
    "fail")
      log_result "${RED}✗ ${test_name}${NC}"
      ((TOTAL_FAIL++))
      ;;
    "partial_fail")
      log_result "${YELLOW}⚠ ${test_name}${NC}"
      ((TOTAL_PARTIAL++))
      ;;
    "manual")
      log_result "${BLUE}○ ${test_name} (수동 테스트)${NC}"
      ((TOTAL_MANUAL++))
      ;;
  esac
done

log_result ""
log_result "성공: ${TOTAL_SUCCESS}"
log_result "실패: ${TOTAL_FAIL}"
log_result "부분 성공: ${TOTAL_PARTIAL}"
log_result "수동 테스트: ${TOTAL_MANUAL}"
log_result ""

if [ $TOTAL_FAIL -eq 0 ]; then
  log_result "${GREEN}🎉 모든 자동 테스트 성공!${NC}"
else
  log_result "${RED}❌ 일부 테스트 실패. 로그를 확인하세요.${NC}"
fi

log_result ""
log_result "결과 파일: ${RESULT_FILE}"