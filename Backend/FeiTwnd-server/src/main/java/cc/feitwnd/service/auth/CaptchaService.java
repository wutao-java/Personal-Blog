package cc.feitwnd.service.auth;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

@Service
public class CaptchaService {

    private static final long CAPTCHA_TTL_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private final Map<String, CaptchaEntry> store = new ConcurrentHashMap<>();
    private final LongSupplier currentTimeMillis;

    public CaptchaService() {
        this(System::currentTimeMillis);
    }

    CaptchaService(LongSupplier currentTimeMillis) {
        this.currentTimeMillis = currentTimeMillis;
    }

    public void put(String captchaId, int result) {
        long expiresAt = currentTimeMillis.getAsLong() + CAPTCHA_TTL_MILLIS;
        store.put(captchaId, new CaptchaEntry(result, expiresAt));
    }

    public boolean verify(String captchaId, int answer) {
        CaptchaEntry entry = store.remove(captchaId);
        return entry != null
                && currentTimeMillis.getAsLong() < entry.expiresAt()
                && entry.result() == answer;
    }

    @Scheduled(fixedRate = 300_000)
    public void cleanExpired() {
        long now = currentTimeMillis.getAsLong();
        store.entrySet().removeIf(entry -> now >= entry.getValue().expiresAt());
    }

    private record CaptchaEntry(int result, long expiresAt) {
    }
}
