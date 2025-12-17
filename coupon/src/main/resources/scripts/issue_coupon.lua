--[[
    -- KEYS[1]: 쿠폰 발급 유저 목록 SET
    -- ARGV[1] = userId (UUID - String)
    -- ARGV[2] = 총 수량 (limit)

    0 성공
    -1 실패: 수량 소진
    -2 실패: 이미 발급 받음
]]

-- < 쿠폰 발급 로직 묶음 >

-- 1. 중복 발급 체크 (Set에 유저 ID가 존재하는 지 확인)
-- SET에 값이 있으면 1, 없으면 0을 반환
if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
    return -2 -- [결과] 이미 발급 받은 유저
end

-- 2. 쿠폰 잔여 수량 조회 및 체크 (local - 지역 변수 선언) SET 0(1)
-- 카운터를 별도로 관리하지 않고, 실제 발급된 유저 수가 곧 수량으로 체크
local current_count = redis.call('SCARD',KEYS[1])

-- 수량이 없거나, 0보다 작거나 같으면 발급 불가 (tonumber - 문자열로 들어온 argv를 숫자로 변환하는 함수)
if tonumber(current_count) >= tonumber(ARGV[2]) then
    return -1 -- [결과] 쿠폰 수량 소진
end

-- 3. 발급 확정 (SET에 추가)
redis.call('SADD', KEYS[1], ARGV[1])

return 0 -- [결과] 쿠폰 발급 성공