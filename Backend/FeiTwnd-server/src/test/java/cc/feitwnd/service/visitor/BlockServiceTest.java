package cc.feitwnd.service.visitor;

import cc.feitwnd.entity.Visitors;
import cc.feitwnd.exception.BlockedException;
import cc.feitwnd.mapper.VisitorMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.longThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BlockServiceTest {

    private VisitorMapper visitorMapper;
    private RedisTemplate<String, Object> redis;
    private ValueOperations<String, Object> valueOperations;
    private BlockService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        visitorMapper = mock(VisitorMapper.class);
        redis = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOperations);
        service = new BlockService(visitorMapper, redis);
    }

    @Test
    void cacheTtlDoesNotOutliveDatabaseBlock() {
        Visitors visitor = Visitors.builder()
                .id(1L)
                .fingerprint("fingerprint")
                .isBlocked(1)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
        when(redis.hasKey("visitor:blocked:fingerprint")).thenReturn(false);
        when(visitorMapper.findVisitorByFingerprint("fingerprint")).thenReturn(visitor);

        assertThrows(BlockedException.class, () -> service.checkIfBlocked("fingerprint"));

        verify(valueOperations).set(
                eq("visitor:blocked:fingerprint"),
                eq("1"),
                longThat(ttl -> ttl > 0 && ttl <= 300),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    void expiredBlockClearsStatusAndExpiryTogether() {
        Visitors visitor = Visitors.builder()
                .id(1L)
                .fingerprint("fingerprint")
                .isBlocked(1)
                .expiresAt(LocalDateTime.now().minusSeconds(1))
                .build();
        when(redis.hasKey("visitor:blocked:fingerprint")).thenReturn(false);
        when(visitorMapper.findVisitorByFingerprint("fingerprint")).thenReturn(visitor);

        service.checkIfBlocked("fingerprint");

        verify(visitorMapper).unblockById(1L);
    }
}
