package cc.wutao.service.auth;

import cc.wutao.constant.JwtClaimsConstant;
import cc.wutao.properties.JwtProperties;
import cc.wutao.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TokenServiceTest {

    private RedisTemplate<String, Object> redis;
    private SetOperations<String, Object> setOperations;
    private JwtProperties jwtProperties;
    private TokenService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redis = mock(RedisTemplate.class);
        setOperations = mock(SetOperations.class);
        when(redis.opsForSet()).thenReturn(setOperations);

        jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey("01234567890123456789012345678901");
        jwtProperties.setTtl(60_000L);
        service = new TokenService(redis, jwtProperties);
    }

    @Test
    void createStoresTokenAndPreservesClaims() {
        String token = service.createAndStoreToken(7L, 1);

        Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
        assertEquals(7, ((Number) claims.get(JwtClaimsConstant.ADMIN_ID)).longValue());
        assertEquals(1, ((Number) claims.get(JwtClaimsConstant.ADMIN_ROLE)).intValue());
        verify(setOperations).add("token:active:7", token);
        verify(redis).expire("token:active:7", 60_000L, TimeUnit.MILLISECONDS);
    }

    @Test
    void validatesAndRevokesExactToken() {
        when(setOperations.isMember("token:active:7", "token-value")).thenReturn(true);

        assertTrue(service.isValidToken(7L, "token-value"));
        service.logout(7L, "token-value");
        service.logoutAll(7L);

        verify(setOperations).remove("token:active:7", "token-value");
        verify(redis).delete("token:active:7");
    }
}
