package cc.wutao.handler;

import cc.wutao.result.Result;
import cc.wutao.utils.AliOssUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OssUrlResponseAdviceTest {

    private static final String RAW_URL = "https://bucket.endpoint/image/cover.webp";
    private static final String SIGNED_URL = "https://signed.example/image/cover.webp?signature=ok";

    private AliOssUtil aliOssUtil;
    private OssUrlResponseAdvice advice;

    @BeforeEach
    void setUp() {
        aliOssUtil = mock(AliOssUtil.class);
        when(aliOssUtil.generateAccessUrls(anyString()))
                .thenAnswer(invocation -> invocation.<String>getArgument(0).replace(RAW_URL, SIGNED_URL));
        advice = new OssUrlResponseAdvice(new ObjectMapper(), aliOssUtil);
    }

    @Test
    void publicApiSignsOssUrlsInNestedFieldsAndMarkdown() {
        Result<Map<String, Object>> body = Result.success(Map.of(
                "records", List.of(Map.of(
                        "coverImage", RAW_URL,
                        "content", "![cover](" + RAW_URL + ")"))));

        JsonNode result = (JsonNode) advice.beforeBodyWrite(
                body,
                null,
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                request("/blog/article/page"),
                mock(ServerHttpResponse.class));

        assertEquals(SIGNED_URL, result.at("/data/records/0/coverImage").asText());
        assertEquals("![cover](" + SIGNED_URL + ")",
                result.at("/data/records/0/content").asText());
    }

    @Test
    void adminApiKeepsCanonicalOssUrls() {
        Result<String> body = Result.success(RAW_URL);

        Object result = advice.beforeBodyWrite(
                body,
                null,
                MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                request("/admin/article/page"),
                mock(ServerHttpResponse.class));

        assertSame(body, result);
        verifyNoInteractions(aliOssUtil);
    }

    private ServerHttpRequest request(String path) {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getURI()).thenReturn(URI.create("http://localhost" + path));
        return request;
    }
}
