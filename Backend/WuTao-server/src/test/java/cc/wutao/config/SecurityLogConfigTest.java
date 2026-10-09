package cc.wutao.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SecurityLogConfigTest {

    @Test
    void retiredHomePrefixDoesNotBypassScanDetection() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/home/.env");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        new SecurityLogConfig().securityScanLogFilter().doFilter(request, response, chain);

        assertEquals(404, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    void sharedApiPrefixesRemainAllowed() throws Exception {
        for (String path : new String[]{"/blog/personalInfo", "/cv/personalInfo", "/admin/socialMedia"}) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            new SecurityLogConfig().securityScanLogFilter().doFilter(request, response, chain);

            assertEquals(200, response.getStatus());
            assertSame(request, chain.getRequest());
        }
    }
}
