package cc.feitwnd.service.visitor;

import cc.feitwnd.exception.UnauthorizedException;
import cc.feitwnd.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VisitorTokenServiceTest {

    private VisitorTokenService service;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecretKey("01234567890123456789012345678901");
        service = new VisitorTokenService(properties);
    }

    @Test
    void resolvesVisitorWhenTokenAndFingerprintMatch() {
        String token = service.generateToken(9L, "fingerprint");
        MockHttpServletRequest request = request(token, "fingerprint");

        assertEquals(9L, service.resolveVisitorId(request));
    }

    @Test
    void rejectsTokenBoundToDifferentFingerprint() {
        String token = service.generateToken(9L, "fingerprint");
        MockHttpServletRequest request = request(token, "other-fingerprint");

        assertThrows(UnauthorizedException.class, () -> service.resolveVisitorId(request));
    }

    @Test
    void rejectsMissingVisitorHeaders() {
        assertThrows(UnauthorizedException.class,
                () -> service.resolveVisitorId(new MockHttpServletRequest()));
    }

    private MockHttpServletRequest request(String token, String fingerprint) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(VisitorTokenService.VISITOR_TOKEN_HEADER, token);
        request.addHeader(VisitorTokenService.VISITOR_FP_HEADER, fingerprint);
        return request;
    }
}
