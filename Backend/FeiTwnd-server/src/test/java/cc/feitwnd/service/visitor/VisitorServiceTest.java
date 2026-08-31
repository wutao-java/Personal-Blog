package cc.feitwnd.service.visitor;

import cc.feitwnd.dto.VisitorRecordDTO;
import cc.feitwnd.entity.Visitors;
import cc.feitwnd.mapper.VisitorMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VisitorServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void concurrentVisitsDoNotLoseViewCount() throws Exception {
        AtomicLong storedViews = new AtomicLong(1);
        CyclicBarrier simultaneousReads = new CyclicBarrier(2);
        VisitorMapper visitorMapper = mock(VisitorMapper.class, invocation -> {
            if ("findVisitorByFingerprint".equals(invocation.getMethod().getName())) {
                long viewsAtRead = storedViews.get();
                simultaneousReads.await(5, TimeUnit.SECONDS);
                return Visitors.builder()
                        .id(1L)
                        .fingerprint("fingerprint")
                        .sessionId("old-session")
                        .ip("127.0.0.1")
                        .lastVisitTime(LocalDateTime.now())
                        .totalViews(viewsAtRead)
                        .isBlocked(0)
                        .build();
            }
            if ("updateById".equals(invocation.getMethod().getName())) {
                storedViews.set(invocation.<Visitors>getArgument(0).getTotalViews());
            }
            if ("recordVisit".equals(invocation.getMethod().getName())) {
                storedViews.incrementAndGet();
            }
            return null;
        });

        RedisTemplate<String, Object> redis = mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOperations);

        FingerprintService fingerprintService = mock(FingerprintService.class);
        when(fingerprintService.generateVisitorFingerprint(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn("fingerprint");

        VisitorService service = new VisitorService(
                visitorMapper,
                mock(AsyncVisitorService.class),
                redis,
                fingerprintService,
                mock(BlockService.class),
                mock(VisitorTokenService.class)
        );
        HttpServletRequest request = request();
        VisitorRecordDTO record = VisitorRecordDTO.builder().pagePath("/").build();

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<?> first = executor.submit(() -> service.recordVisitorViewInfo(record, request));
            Future<?> second = executor.submit(() -> service.recordVisitorViewInfo(record, request));
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        }

        assertEquals(3, storedViews.get());
    }

    private HttpServletRequest request() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(request.getHeader("User-Agent")).thenReturn("test-agent");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(session.getId()).thenReturn("session");
        return request;
    }
}
