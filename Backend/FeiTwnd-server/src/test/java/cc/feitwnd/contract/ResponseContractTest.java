package cc.feitwnd.contract;

import cc.feitwnd.result.PageResult;
import cc.feitwnd.result.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldKeepSuccessResponseShape() {
        JsonNode json = objectMapper.valueToTree(Result.success("payload"));

        assertEquals(1, json.get("code").asInt());
        assertTrue(json.get("msg").isNull());
        assertEquals("payload", json.get("data").asText());
    }

    @Test
    void shouldKeepErrorResponseShape() {
        JsonNode json = objectMapper.valueToTree(Result.error("failed"));

        assertEquals(0, json.get("code").asInt());
        assertEquals("failed", json.get("msg").asText());
        assertTrue(json.get("data").isNull());
    }

    @Test
    void shouldKeepPageResponseShape() {
        JsonNode json = objectMapper.valueToTree(new PageResult<>(2, List.of("a", "b")));

        assertEquals(2, json.get("total").asLong());
        assertEquals(List.of("a", "b"), objectMapper.convertValue(json.get("records"), List.class));
    }
}
