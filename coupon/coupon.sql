-- =============================================
-- 쿠폰 더미 데이터 (임시 테스트용) id, createBy 랜덤, UTC 시간으로 저장
-- =============================================

-- 1. 연말 세일 쿠폰
-- 유효기간: 2025-12-31까지
-- 총 수량: 1000개 (1인 1회 발급 가능)
-- 할인율: 30%
INSERT INTO p_coupon (
    id, name, description, discount_rate, total_quantity,
    created_at, updated_at, deleted_at, issue_start_at, issue_end_at, valid_until,
    created_by, updated_by, deleted_by
)
VALUES (
           UNHEX(REPLACE(UUID(),'-','')), -- id
           '연말 세일 쿠폰',              -- name
           '2025년 연말까지 사용 가능한 30% 할인 쿠폰 [전상품 적용]', -- description
           30.00,                         -- discount_rate
           1000,                           -- total_quantity
           NOW(),                         -- created_at
           NULL,                          -- updated_at
           NULL,                          -- deleted_at
           NOW(),                         -- issue_start_at
           '2025-12-31 23:59:59',         -- issue_end_at
           '2025-12-31 23:59:59',         -- valid_until
           UNHEX(REPLACE(UUID(),'-','')), -- created_by (랜덤)
           NULL,                          -- updated_by
           NULL                           -- deleted_by
       );

-- 2. 깜짝 쿠폰 발급 이벤트
-- 유효기간: 2025-12-10까지 (12-08 발급 종료)
-- 총 수량: 500 (1인 1회 발급 가능)
-- 할인율: 25%
INSERT INTO p_coupon (
    id, name, description, discount_rate, total_quantity,
    created_at, updated_at, deleted_at, issue_start_at, issue_end_at, valid_until,
    created_by, updated_by, deleted_by
)
VALUES (
           UNHEX(REPLACE(UUID(),'-','')),
           '깜짝 쿠폰',
           '타임 어택 깜짝 쿠폰 [전상품 적용]',
           25.00,
           500,
           NOW(),
           NULL,
           NULL,
           NOW(),
           '2025-12-08 23:59:59',
           '2025-12-10 23:59:59',
           UNHEX(REPLACE(UUID(),'-','')),
           NULL,
           NULL
       );