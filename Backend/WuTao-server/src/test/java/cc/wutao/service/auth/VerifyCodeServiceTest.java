package cc.wutao.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerifyCodeServiceTest {

    private RedisTemplate<String, Object> redis;
    private ValueOperations<String, Object> valueOperations;
    private VerifyCodeService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redis = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOperations);
        service = new VerifyCodeService(redis);
    }

    @Test
    void generatedCodeAlwaysHasSixDigits() {
        for (int i = 0; i < 100; i++) {
            assertTrue(service.generateCode().matches("\\d{6}"));
        }
    }

    @Test
    void saveUsesExistingRedisKeysAndDoesNotClearSecurityLock() {
        service.saveCode("123456");

        verify(valueOperations).set("verify_code", "123456", 5, TimeUnit.MINUTES);
        verify(valueOperations).set("rate_limit", "1", 60, TimeUnit.SECONDS);
        verify(redis).delete("attempt_count");
        verify(redis, never()).delete("lock");
    }

    @Test
    void successfulVerificationConsumesAllState() {
        when(redis.hasKey("lock")).thenReturn(false);
        when(valueOperations.get("verify_code")).thenReturn("123456");

        assertTrue(service.verifyCode(" 123456 "));
        verify(redis).delete("verify_code");
        verify(redis).delete("rate_limit");
        verify(redis).delete("attempt_count");
        verify(redis).delete("lock");
    }

    @Test
    void fifthFailureLocksFurtherAttempts() {
        when(redis.hasKey("lock")).thenReturn(false);
        when(valueOperations.get("verify_code")).thenReturn("123456");
        when(valueOperations.increment("attempt_count", 1L)).thenReturn(5L);

        assertFalse(service.verifyCode("000000"));
        verify(valueOperations).set("lock", "1", 30, TimeUnit.MINUTES);
    }

    @Test
    void redisFailureDoesNotResetAttemptCount() {
        when(valueOperations.get("attempt_count")).thenThrow(new IllegalStateException("redis unavailable"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, service::getAttemptCount);
        assertEquals("redis unavailable", exception.getMessage());
    }
}
