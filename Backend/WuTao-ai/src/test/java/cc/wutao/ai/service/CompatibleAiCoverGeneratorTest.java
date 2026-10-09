package cc.wutao.ai.service;

import cc.wutao.ai.properties.AiImageProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CompatibleAiCoverGeneratorTest {

    private HttpServer server;
    private AiImageProperties properties;
    private ObjectMapper objectMapper;
    private final AtomicReference<JsonNode> request = new AtomicReference<>();
    private final AtomicReference<String> auth = new AtomicReference<>();
    private String response = "{\"data\":[{\"b64_json\":\"cG5n\"}]}";
    private int status = 200;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/images/generations", exchange -> {
            auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            request.set(objectMapper.readTree(exchange.getRequestBody()));
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        properties = new AiImageProperties();
        properties.setEnabled(true);
        properties.setEndpoint("http://127.0.0.1:" + server.getAddress().getPort() + "/images/generations");
        properties.setApiKey("test-key");
        properties.setModelName("configured-image-model");
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void sendsIndependentImageConfigurationAndDecodesBase64() {
        CompatibleAiCoverGenerator generator = new CompatibleAiCoverGenerator(properties, objectMapper);
        assertArrayEquals("png".getBytes(StandardCharsets.UTF_8), generator.generate("测试标题", "# 测试正文"));
        assertEquals("Bearer test-key", auth.get());
        assertEquals("configured-image-model", request.get().path("model").asText());
        assertTrue(request.get().path("prompt").asText().contains("测试标题"));
        assertTrue(request.get().path("prompt").asText().contains("测试正文"));
        assertEquals(1, request.get().path("n").asInt());
        assertFalse(request.get().has("response_format"));
    }

    @Test
    void responseFormatIsOptInAndProviderErrorsAreNotRetried() {
        properties.setResponseFormat("b64_json");
        status = 429;
        var generator = new CompatibleAiCoverGenerator(properties, objectMapper);
        assertThrows(IllegalStateException.class, () -> generator.generate("title", "content"));
        assertEquals("b64_json", request.get().path("response_format").asText());
    }

    @Test
    void remoteImageUrlsAreRejectedWithoutDownloadingThem() {
        response = "{\"data\":[{\"url\":\"http://127.0.0.1/private\"}]}";
        var generator = new CompatibleAiCoverGenerator(properties, objectMapper);
        assertThrows(IllegalStateException.class, () -> generator.generate("title", "content"));
    }

    @Test
    void missingModelDoesNotReportAvailable() {
        properties.setModelName("");
        assertFalse(new CompatibleAiCoverGenerator(properties, objectMapper).isAvailable());
    }
}
