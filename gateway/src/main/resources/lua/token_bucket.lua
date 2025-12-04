-- Token Bucket Rate Limiting Algorithm
-- Ensures atomic operations for distributed rate limiting

-- KEYS[1]: Redis key for the bucket (e.g., rate_limit:user:{userId})
-- ARGV[1]: max_tokens - Maximum bucket capacity (e.g., 100)
-- ARGV[2]: refill_rate - Tokens added per second (e.g., 10)
-- ARGV[3]: requested_tokens - Tokens to consume for this request (e.g., 1)
-- ARGV[4]: current_timestamp_millis - Current time in milliseconds

local key = KEYS[1]
local max_tokens = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local requested = tonumber(ARGV[3])
local now = tonumber(ARGV[4])

-- Get current bucket state from Redis
local bucket = redis.call('GET', key)
local tokens = max_tokens
local last_refill = now

-- If bucket exists, parse current state
if bucket then
    local data = cjson.decode(bucket)
    tokens = tonumber(data.tokens)
    last_refill = tonumber(data.lastRefillTimestamp)

    -- Calculate refilled tokens based on elapsed time
    local elapsed_seconds = (now - last_refill) / 1000
    local refilled = math.floor(elapsed_seconds * refill_rate)

    -- Add refilled tokens, capped at max_tokens
    tokens = math.min(max_tokens, tokens + refilled)

    -- Update last refill time to now
    last_refill = now
end

-- Try to consume the requested tokens
if tokens >= requested then
    -- Enough tokens available
    tokens = tokens - requested

    -- Save updated bucket state with TTL of 120 seconds
    local new_bucket = cjson.encode({
        tokens = tokens,
        lastRefillTimestamp = last_refill
    })
    redis.call('SETEX', key, 120, new_bucket)

    -- Return remaining tokens (success)
    return tokens
else
    -- Not enough tokens available
    return -1
end