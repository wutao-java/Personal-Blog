package cc.feitwnd.config;

import cc.feitwnd.result.PageResult;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RedisConfigurationTest {

    private final GenericJackson2JsonRedisSerializer serializer =
            new RedisConfiguration().jackson2JsonRedisSerializer();

    @Test
    void shouldRoundTripCachedApplicationObjects() {
        PageResult<String> value = new PageResult<>(2, List.of("first", "second"));

        Object restored = serializer.deserialize(serializer.serialize(value));

        PageResult<?> page = assertInstanceOf(PageResult.class, restored);
        assertEquals(2, page.getTotal());
        assertEquals(List.of("first", "second"), page.getRecords());
    }

    @Test
    @SuppressWarnings("deprecation")
    void shouldReadValuesWrittenByLegacySerializer() {
        ObjectMapper legacyMapper = new ObjectMapper();
        legacyMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY);
        legacyMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        GenericJackson2JsonRedisSerializer legacySerializer =
                new GenericJackson2JsonRedisSerializer(legacyMapper);

        byte[] legacyValue = legacySerializer.serialize(
                new PageResult<>(1, List.of("cached")));
        Object restored = serializer.deserialize(legacyValue);

        PageResult<?> page = assertInstanceOf(PageResult.class, restored);
        assertEquals(1, page.getTotal());
        assertEquals(List.of("cached"), page.getRecords());
    }
}
