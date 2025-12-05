#!/bin/bash

# 타임스탬프 생성
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# 결과 파일 경로 (scripts 폴더 내)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESULT_FILE="${SCRIPT_DIR}/user-crud-test-result-${TIMESTAMP}.txt"

BASE_URL="http://localhost:8000/api/v1/users"
EMAIL="testuser$(date +%s)@example.com"
PASSWORD="Test1234@"
NEW_PASSWORD="NewPass1234@"

{
echo "======================================"
echo "User Service CRUD API Test"
echo "======================================"
echo "Results will be saved to: ${RESULT_FILE}"
echo ""

# Test 1: Signup
echo "[Test 1] Signup"
curl -s -X POST "$BASE_URL/signup" \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"$EMAIL\", \"password\": \"$PASSWORD\", \"name\": \"TestUser\"}"
echo ""
echo ""

# Test 2: Login
echo "[Test 2] Login"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"$EMAIL\", \"password\": \"$PASSWORD\"}")
echo "$LOGIN_RESPONSE"
TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
echo ""
echo ""

# Test 3: Get My Info
echo "[Test 3] Get My Info"
curl -s -X GET "$BASE_URL/me" -H "Authorization: Bearer $TOKEN"
echo ""
echo ""

# Test 4: Update User Info (All Fields)
echo "[Test 4] Update User Info (All Fields)"
curl -s -X PUT "$BASE_URL/me" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "UpdatedName", "phoneNumber": "010-9999-8888", "deliveryAddress": "Seoul Seocho", "detailAddress": "202-202"}'
echo ""
echo ""

# Test 5: Update User Info (Name Only)
echo "[Test 5] Update User Info (Name Only)"
curl -s -X PUT "$BASE_URL/me" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "NameOnly"}'
echo ""
echo ""

# Test 6: Change Password
echo "[Test 6] Change Password"
curl -s -X PATCH "$BASE_URL/me/password" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"currentPassword\": \"$PASSWORD\", \"newPassword\": \"$NEW_PASSWORD\"}"
echo ""
echo ""

# Test 7: Re-login with New Password
echo "[Test 7] Re-login with New Password"
LOGIN_RESPONSE2=$(curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"$EMAIL\", \"password\": \"$NEW_PASSWORD\"}")
echo "$LOGIN_RESPONSE2"
TOKEN=$(echo "$LOGIN_RESPONSE2" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
echo ""
echo ""

# Test 8: Delete Account (204 Expected)
echo "[Test 8] Delete Account (204 Expected)"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/me" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"password\": \"$NEW_PASSWORD\"}")
echo "Status: $STATUS"
echo ""

# Test 9: Re-login with Deleted Account (Fail Expected)
echo "[Test 9] Re-login with Deleted Account (Fail Expected)"
curl -s -X POST "$BASE_URL/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"$EMAIL\", \"password\": \"$NEW_PASSWORD\"}"
echo ""
echo ""

# Test 10: Access without Auth (401 Expected)
echo "[Test 10] Access without Auth (401 Expected)"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/me")
echo "Status: $STATUS"
echo ""

echo "======================================"
echo "Test Completed"
echo "Results saved to: ${RESULT_FILE}"
echo "======================================"
} | tee "${RESULT_FILE}"
