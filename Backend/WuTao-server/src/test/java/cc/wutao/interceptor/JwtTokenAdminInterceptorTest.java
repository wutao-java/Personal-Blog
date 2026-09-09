package cc.wutao.interceptor;

import cc.wutao.constant.JwtClaimsConstant;
import cc.wutao.constant.StatusConstant;
import cc.wutao.context.BaseContext;
import cc.wutao.exception.GuestReadOnlyException;
import cc.wutao.exception.UnauthorizedException;
import cc.wutao.properties.JwtProperties;
import cc.wutao.service.auth.TokenService;
import cc.wutao.utils.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenAdminInterceptorTest {

    private JwtProperties jwtProperties;
    private TokenService tokenService;
    private JwtTokenAdminInterceptor interceptor;
    private HandlerMethod handler;

    @BeforeEach
    void setUp() throws Exception {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey("01234567890123456789012345678901");
        jwtProperties.setTokenName("Authorization");
        tokenService = mock(TokenService.class);
        interceptor = new JwtTokenAdminInterceptor(jwtProperties, tokenService);

        Method method = getClass().getDeclaredMethod("endpoint");
        handler = new HandlerMethod(this, method);
    }

    @AfterEach
    void clearContext() {
        BaseContext.removeCurrentId();
        BaseContext.removeCurrentRole();
    }

    @Test
    void validTokenPopulatesAndThenClearsRequestContext() throws Exception {
        String token = token(7L, 1);
        when(tokenService.isValidToken(7L, token)).thenReturn(true);
        MockHttpServletRequest request = request("GET", token);

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), handler));
        assertEquals(7L, BaseContext.getCurrentId());
        assertEquals(1, BaseContext.getCurrentRole());

        interceptor.afterCompletion(request, new MockHttpServletResponse(), handler, null);
        assertNull(BaseContext.getCurrentId());
        assertNull(BaseContext.getCurrentRole());
    }

    @Test
    void tokenMissingFromRedisIsRejectedWithoutContext() {
        String token = token(7L, 1);
        when(tokenService.isValidToken(7L, token)).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> interceptor.preHandle(request("GET", token), new MockHttpServletResponse(), handler));
        assertNull(BaseContext.getCurrentId());
        assertNull(BaseContext.getCurrentRole());
    }

    @Test
    void guestCannotModifyAdminResources() {
        String token = token(7L, StatusConstant.DISABLE);
        when(tokenService.isValidToken(7L, token)).thenReturn(true);

        assertThrows(GuestReadOnlyException.class,
                () -> interceptor.preHandle(request("POST", token), new MockHttpServletResponse(), handler));
    }

    private MockHttpServletRequest request(String method, String token) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/admin/test");
        request.addHeader(jwtProperties.getTokenName(), token);
        return request;
    }

    private String token(Long adminId, Integer role) {
        return JwtUtil.createJWT(jwtProperties.getSecretKey(), 60_000L, Map.of(
                JwtClaimsConstant.ADMIN_ID, adminId,
                JwtClaimsConstant.ADMIN_ROLE, role
        ));
    }

    @SuppressWarnings("unused")
    private void endpoint() {
    }
}
