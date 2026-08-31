package cc.feitwnd.task;

import cc.feitwnd.mapper.ArticleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewCountSyncTaskTest {

    private RedisTemplate<String, Object> redis;
    private ValueOperations<String, Object> valueOperations;
    private HashOperations<String, Object, Object> hashOperations;
    private ArticleMapper articleMapper;
    private ViewCountSyncTask task;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redis = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        hashOperations = mock(HashOperations.class);
        articleMapper = mock(ArticleMapper.class);
        when(redis.opsForValue()).thenReturn(valueOperations);
        when(redis.opsForHash()).thenReturn(hashOperations);
        task = new ViewCountSyncTask(redis, articleMapper);
    }

    @Test
    void zeroRemainderIsKeptToAvoidRacingWithNewViews() {
        when(valueOperations.setIfAbsent("lock:viewCountSync", "1", Duration.ofMinutes(4)))
                .thenReturn(true);
        when(hashOperations.entries("article:viewCount")).thenReturn(Map.of("10", 5));
        when(hashOperations.increment("article:viewCount", "10", -5)).thenReturn(0L);

        task.syncViewCountToMySQL();

        verify(articleMapper).addViewCount(10L, 5);
        verify(hashOperations, never()).delete("article:viewCount", "10");
        verify(redis).delete("lock:viewCountSync");
    }
}
