package cc.feitwnd.service.auth;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaptchaServiceTest {

    @Test
    void captchaCanOnlyBeVerifiedOnce() {
        CaptchaService service = new CaptchaService();
        service.put("captcha-1", 12);

        assertTrue(service.verify("captcha-1", 12));
        assertFalse(service.verify("captcha-1", 12));
    }

    @Test
    void wrongAnswerConsumesCaptcha() {
        CaptchaService service = new CaptchaService();
        service.put("captcha-1", 12);

        assertFalse(service.verify("captcha-1", 11));
        assertFalse(service.verify("captcha-1", 12));
    }

    @Test
    void captchaExpiresAfterFiveMinutes() {
        AtomicLong now = new AtomicLong(1_000L);
        CaptchaService service = new CaptchaService(now::get);
        service.put("captcha-1", 12);

        now.addAndGet(Duration.ofMinutes(5).toMillis());

        assertFalse(service.verify("captcha-1", 12));
    }

    @Test
    void cleanupKeepsFreshCaptchasUnderLoad() {
        CaptchaService service = new CaptchaService();
        for (int i = 0; i <= 1_000; i++) {
            service.put("captcha-" + i, i);
        }

        service.cleanExpired();

        assertTrue(service.verify("captcha-0", 0));
        assertTrue(service.verify("captcha-1000", 1000));
    }
}
