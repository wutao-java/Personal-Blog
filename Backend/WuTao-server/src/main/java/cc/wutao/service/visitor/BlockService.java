package cc.wutao.service.visitor;

import cc.wutao.constant.MessageConstant;
import cc.wutao.constant.StatusConstant;
import cc.wutao.entity.Visitors;
import cc.wutao.exception.BlockedException;
import cc.wutao.mapper.VisitorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockService {

    private final VisitorMapper visitorMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RATE_LIMIT_KEY = "visitor:rate:";
    private static final String BLOCKED_KEY = "visitor:blocked:";

    /**
     * 检查缓存是否有被封禁记录
     * @param fingerprint
     */
    public void checkIfBlocked(String fingerprint) {
        // 先检查Redis缓存
        String blockedKey = BLOCKED_KEY + fingerprint;
        Boolean isBlocked = redisTemplate.hasKey(blockedKey);

        if(Boolean.TRUE.equals(isBlocked)){
            throw new BlockedException(MessageConstant.VISITOR_BLOCKED);
        }
        // 检查数据库
        Visitors visitor = visitorMapper.findVisitorByFingerprint(fingerprint);
        LocalDateTime now = LocalDateTime.now();
        if (visitor != null && StatusConstant.ENABLE.equals(visitor.getIsBlocked())) {
            LocalDateTime expiresAt = visitor.getExpiresAt();
            if (expiresAt == null || expiresAt.isAfter(now)) {
                // 封禁有效，更新Redis缓存
                long ttlSeconds = expiresAt == null
                        ? TimeUnit.DAYS.toSeconds(1)
                        : Math.max(1, Duration.between(now, expiresAt).toSeconds());
                redisTemplate.opsForValue().set(blockedKey, "1", ttlSeconds, TimeUnit.SECONDS);
                throw new BlockedException(MessageConstant.VISITOR_BLOCKED);
            } else {
                // 封禁已过期，解除封禁
                log.info("【访客追踪】封禁过期自动解封: id={}, fingerprint={}, expiresAt={}",
                        visitor.getId(), fingerprint, visitor.getExpiresAt());
                visitorMapper.unblockById(visitor.getId());
            }
        }
    }

    /**
     * 检查请求频率
     * @param fingerprint
     * @param ip
     */
    public void checkRateLimit(String fingerprint, String ip) {
        // IP级别限制：每分钟60次
        String ipKey = RATE_LIMIT_KEY + "ip:" + ip;
        Long ipCount = redisTemplate.opsForValue().increment(ipKey, 1);
        if (ipCount == 1) {
            redisTemplate.expire(ipKey, 1, TimeUnit.MINUTES);
        }
        if (ipCount > 60) {
            // 自动封禁
            blockVisitor(fingerprint);
            throw new BlockedException(MessageConstant.VISITOR_BLOCKED);
        }

        // 指纹级别限制：每小时1000次
        String fpKey = RATE_LIMIT_KEY + "fp:" + fingerprint;
        Long fpCount = redisTemplate.opsForValue().increment(fpKey, 1);
        if (fpCount == 1) {
            redisTemplate.expire(fpKey, 1, TimeUnit.HOURS);
        }
        if (fpCount > 1000) {
            // 自动封禁
            blockVisitor(fingerprint);
            throw new BlockedException(MessageConstant.VISITOR_BLOCKED);
        }
    }

    /**
     * 封禁访客
     * @param fingerprint
     */
    private void blockVisitor(String fingerprint) {
        Visitors visitor = visitorMapper.findVisitorByFingerprint(fingerprint);
        if (visitor != null) {
            visitor.setIsBlocked(1);
            // 封1天
            visitor.setExpiresAt(LocalDateTime.now().plusDays(1));
            visitorMapper.updateById(visitor);

            // 更新Redis缓存
            String blockedKey = BLOCKED_KEY + fingerprint;
            redisTemplate.opsForValue().set(blockedKey, "1", 1, TimeUnit.DAYS);
            log.warn("封禁访客: fingerprint={}",
                    fingerprint);
        }
    }
}
