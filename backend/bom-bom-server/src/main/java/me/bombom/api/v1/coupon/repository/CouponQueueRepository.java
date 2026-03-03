package me.bombom.api.v1.coupon.repository;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponQueueRepository {

    private static final String QUEUE_KEY_PREFIX = "coupon:queue:";
    private static final String QUEUE_SEQUENCE_KEY_PREFIX = "coupon:queue:seq:";
    private static final String ISSUED_COUNT_KEY_PREFIX = "coupon:issuedCount:";
    private static final String ACTIVE_KEY_PREFIX = "coupon:active:";
    private static final String ISSUED_KEY_PREFIX = "coupon:issued:";
    private static final String SOLD_OUT_KEY_PREFIX = "coupon:soldOut:";
    private static final RedisScript<Long> PROMOTE_QUEUE_TO_ACTIVE_SCRIPT = buildPromoteQueueToActiveScript();
    private static final RedisScript<Long> ADD_QUEUE_ATOMIC_SCRIPT = buildAddQueueAtomicScript();

    private final StringRedisTemplate redisTemplate;

    private String slotTag(String couponName) {
        return "{" + couponName + "}";
    }

    private String queueKey(String couponName) {
        return QUEUE_KEY_PREFIX + slotTag(couponName);
    }

    private String queueSequenceKey(String couponName) {
        return QUEUE_SEQUENCE_KEY_PREFIX + slotTag(couponName);
    }

    private String issuedCountKey(String couponName) {
        return ISSUED_COUNT_KEY_PREFIX + slotTag(couponName);
    }

    private String activeKey(String couponName) {
        return ACTIVE_KEY_PREFIX + slotTag(couponName);
    }

    private String issuedKey(String couponName) {
        return ISSUED_KEY_PREFIX + slotTag(couponName);
    }

    private String soldOutKey(String couponName) {
        return SOLD_OUT_KEY_PREFIX + slotTag(couponName);
    }

    /**
     * 대기열에 이미 없다면 추가합니다. (ZADD NX)
     *
     * @return true: 새로 추가됨, false: 이미 존재하여 추가되지 않음
     */
    public boolean addIfAbsentQueue(String couponName, Long memberId, double score) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Boolean added = zSetOps.addIfAbsent(queueKey(couponName), memberId.toString(), score);
        return Boolean.TRUE.equals(added);
    }

    /**
     * 대기열에 점수(score) 없이 등록합니다.
     * 기존 addIfAbsentQueue(score)보다 경합에서 더 엄격한 멱등/공정성 보장을 위해
     * 회원 존재 여부 + 순번 채번 + 큐 삽입을 Lua 스크립트로 원자적으로 처리합니다.
     *
     * @return true: 새로 추가됨, false: 이미 존재하여 추가되지 않음
     */
    public boolean addIfAbsentQueue(String couponName, Long memberId) {
        Long result = redisTemplate.execute(
                ADD_QUEUE_ATOMIC_SCRIPT,
                List.of(queueKey(couponName), queueSequenceKey(couponName)),
                memberId.toString()
        );
        return result != null && result == 1L;
    }

    public QueuePreState getQueuePreState(String couponName, Long memberId) {
        List<Object> results = redisTemplate.executePipelined(
                new SessionCallback<Object>() {
                    @Override
                    public Object execute(RedisOperations operations) {
                        operations.opsForZSet().zCard(activeKey(couponName));
                        operations.opsForSet().isMember(issuedKey(couponName), memberId.toString());
                        operations.opsForValue().get(soldOutKey(couponName));
                        operations.opsForZSet().score(activeKey(couponName), memberId.toString());
                        operations.opsForZSet().rank(queueKey(couponName), memberId.toString());
                        operations.opsForZSet().zCard(queueKey(couponName));
                        return null;
                    }
                }
        );

        long activeCount = asLong(results.get(0));
        boolean isIssued = asBoolean(results.get(1));
        boolean isSoldOut = "1".equals(asText(results.get(2)));
        Long activeExpireAt = asLong(results.get(3));
        Long rank = asLong(results.get(4));
        long queueCount = asLong(results.get(5));

        return new QueuePreState(activeCount, isIssued, isSoldOut, activeExpireAt, rank, queueCount);
    }

    public QueuePostState getQueuePostState(String couponName, Long memberId) {
        List<Object> results = redisTemplate.executePipelined(
                new SessionCallback<Object>() {
                    @Override
                    public Object execute(RedisOperations operations) {
                        operations.opsForZSet().score(activeKey(couponName), memberId.toString());
                        operations.opsForZSet().rank(queueKey(couponName), memberId.toString());
                        operations.opsForZSet().zCard(queueKey(couponName));
                        operations.opsForZSet().zCard(activeKey(couponName));
                        return null;
                    }
                }
        );

        Long activeExpireAt = asLong(results.get(0)) != null ? asLong(results.get(0)) : null;
        Long rank = asLong(results.get(1));
        long queueCount = asLong(results.get(2));
        long activeCount = asLong(results.get(3));

        return new QueuePostState(activeExpireAt, rank, queueCount, activeCount);
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Double doubleValue) {
            return doubleValue.longValue();
        }
        if (value instanceof String stringValue) {
            try {
                return Double.valueOf(stringValue).longValue();
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (value instanceof byte[] bytes) {
            try {
                return Double.valueOf(new String(bytes)).longValue();
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String asText(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }
        if (value instanceof byte[] bytes) {
            return new String(bytes);
        }
        return String.valueOf(value);
    }

    private boolean asBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String stringValue) {
            return Boolean.parseBoolean(stringValue);
        }
        if (value instanceof byte[] bytes) {
            return Boolean.parseBoolean(new String(bytes));
        }
        return false;
    }

    /**
     * 대기열에서 [start, end] 구간의 memberId를 조회합니다.
     * 순서는 선착순(Score, Value 순)입니다.
     */
    public Set<Long> rangeQueue(String couponName, long start, long end) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Set<String> ranges = zSetOps.range(queueKey(couponName), start, end);
        if (ranges == null || ranges.isEmpty()) {
            return Collections.emptySet();
        }
        return ranges.stream()
                .map(Long::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * active에서 [start, end] 구간의 memberId를 조회합니다.
     */
    public Set<Long> rangeActive(String couponName, long start, long end) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Set<String> ranges = zSetOps.range(activeKey(couponName), start, end);
        if (ranges == null || ranges.isEmpty()) {
            return Collections.emptySet();
        }
        return ranges.stream()
                .map(Long::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 대기열에서 해당 사용자의 순위(0-based)를 조회합니다.
     */
    public Long rankQueue(String couponName, Long memberId) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.rank(queueKey(couponName), memberId.toString());
    }

    /**
     * 대기열 전체 크기.
     */
    public long getQueueCount(String couponName) {
        Long count = redisTemplate.opsForZSet().zCard(queueKey(couponName));
        return count != null ? count : 0L;
    }

    /**
     * 대기열에서 해당 사용자를 제거합니다.
     */
    public void removeQueue(String couponName, Long memberId) {
        redisTemplate.opsForZSet().remove(queueKey(couponName), memberId.toString());
    }

    /**
     * 만료된 active 멤버를 제거합니다. (score <= nowMillis)
     */
    public long removeExpiredActive(String couponName, double nowMillis) {
        Long removed = redisTemplate.opsForZSet()
                .removeRangeByScore(activeKey(couponName), Double.NEGATIVE_INFINITY, nowMillis);
        return removed != null ? removed : 0L;
    }

    /**
     * active에 추가합니다. score는 만료 시각(epoch millis)입니다.
     */
    public boolean addActive(String couponName, Long memberId, double expireAtMillis) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Boolean added = zSetOps.add(activeKey(couponName), memberId.toString(), expireAtMillis);
        return Boolean.TRUE.equals(added);
    }

    /**
     * active에 존재하는지 확인합니다.
     */
    public boolean isActive(String couponName, Long memberId) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Double score = zSetOps.score(activeKey(couponName), memberId.toString());
        return score != null;
    }

    /**
     * active 만료 시각(epoch millis)을 조회합니다.
     */
    public Long getActiveExpireAtMillis(String couponName, Long memberId) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        Double score = zSetOps.score(activeKey(couponName), memberId.toString());
        return score != null ? score.longValue() : null;
    }

    /**
     * active에서 제거합니다.
     */
    public void removeActive(String couponName, Long memberId) {
        redisTemplate.opsForZSet().remove(activeKey(couponName), memberId.toString());
    }

    /**
     * active 인원 수를 반환합니다.
     */
    public long getActiveCount(String couponName) {
        Long count = redisTemplate.opsForZSet().zCard(activeKey(couponName));
        return count != null ? count : 0L;
    }

    /**
     * 발급 완료 멤버를 저장합니다. (SADD)
     *
     * @return true: 새로 추가됨, false: 이미 존재
     */
    public boolean addIssued(String couponName, Long memberId) {
        Long added = redisTemplate.opsForSet().add(issuedKey(couponName), memberId.toString());
        return added != null && added == 1L;
    }

    /**
     * 발급 완료 여부 확인.
     */
    public boolean isIssued(String couponName, Long memberId) {
        Boolean member = redisTemplate.opsForSet().isMember(issuedKey(couponName), memberId.toString());
        return Boolean.TRUE.equals(member);
    }

    /**
     * 대기열에서 앞사람부터 active로 승격합니다. (원자적)
     *
     * @return active로 승격된 인원 수
     */
    public long promoteQueueToActive(String couponName, long count, long expireAtMillis) {
        if (count <= 0) {
            return 0L;
        }

        Long result = redisTemplate.execute(
                PROMOTE_QUEUE_TO_ACTIVE_SCRIPT,
                List.of(queueKey(couponName), activeKey(couponName), issuedKey(couponName)),
                String.valueOf(count),
                String.valueOf(expireAtMillis)
        );
        return result != null ? result : 0L;
    }

    /**
     * 현재까지 발급된 수를 조회합니다. 값이 없으면 0을 반환합니다.
     */
    public long getIssuedCount(String couponName) {
        String value = redisTemplate.opsForValue().get(issuedCountKey(couponName));
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public boolean isSoldOut(String couponName) {
        String value = redisTemplate.opsForValue().get(soldOutKey(couponName));
        return "1".equals(value);
    }

    public void markSoldOut(String couponName) {
        redisTemplate.opsForValue().set(soldOutKey(couponName), "1");
    }

    public void clearSoldOut(String couponName) {
        redisTemplate.delete(soldOutKey(couponName));
    }

    public void clearEventState(String couponName) {
        redisTemplate.delete(queueKey(couponName));
        redisTemplate.delete(activeKey(couponName));
        redisTemplate.delete(issuedKey(couponName));
        redisTemplate.delete(issuedCountKey(couponName));
        redisTemplate.delete(queueSequenceKey(couponName));
        redisTemplate.delete(soldOutKey(couponName));
    }

    /**
     * 발급 수를 증가시키고, 증가 후 값을 반환합니다.
     */
    public long increaseIssuedCount(String couponName, long delta) {
        Long result = redisTemplate.opsForValue().increment(issuedCountKey(couponName), delta);
        return result != null ? result : 0L;
    }

    private static RedisScript<Long> buildPromoteQueueToActiveScript() {
        String script = String.join("\n",
                "local queue = KEYS[1]",
                "local active = KEYS[2]",
                "local issued = KEYS[3]",
                "local needed = tonumber(ARGV[1])",
                "local expireAt = ARGV[2]",
                "local added = 0",
                "while added < needed do",
                "  local batch = redis.call('ZRANGE', queue, 0, needed - 1)",
                "  if #batch == 0 then",
                "    break",
                "  end",
                "  for i, member in ipairs(batch) do",
                "    if added >= needed then",
                "      break",
                "    end",
                "    if redis.call('SISMEMBER', issued, member) == 1 then",
                "      redis.call('ZREM', queue, member)",
                "    else",
                "      redis.call('ZREM', queue, member)",
                "      redis.call('ZADD', active, expireAt, member)",
                "      added = added + 1",
                "    end",
                "  end",
                "end",
                "return added"
        );

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptText(script);
        return redisScript;
    }

    private static RedisScript<Long> buildAddQueueAtomicScript() {
        String script = String.join("\n",
                "local queueKey = KEYS[1]",
                "local seqKey = KEYS[2]",
                "local memberId = ARGV[1]",
                "if redis.call('ZSCORE', queueKey, memberId) ~= false then",
                "  return 0",
                "end",
                "local score = redis.call('INCR', seqKey)",
                "redis.call('ZADD', queueKey, score, memberId)",
                "return 1"
        );
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptText(script);
        return redisScript;
    }

    public record QueuePreState(
            long activeCount,
            boolean issued,
            boolean soldOut,
            Long activeExpireAt,
            Long queueRank,
            long queueCount
    ) {
    }

    public record QueuePostState(
            Long activeExpireAt,
            Long queueRank,
            long queueCount,
            long activeCount
    ) {
    }
}
