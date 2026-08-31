package cc.feitwnd.handler;

import cc.feitwnd.result.Result;
import cc.feitwnd.utils.AliOssUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class OssUrlResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;
    private final AliOssUtil aliOssUtil;

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (!(body instanceof Result<?>) || !isPublicApi(request)) {
            return body;
        }

        JsonNode root = objectMapper.valueToTree(body);
        replaceOssUrls(root);
        return root;
    }

    private boolean isPublicApi(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return path.startsWith("/blog/")
                || path.startsWith("/home/")
                || path.startsWith("/cv/");
    }

    private void replaceOssUrls(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            for (Map.Entry<String, JsonNode> field : objectNode.properties()) {
                JsonNode value = field.getValue();
                if (value.isTextual()) {
                    objectNode.put(field.getKey(), aliOssUtil.generateAccessUrls(value.textValue()));
                } else {
                    replaceOssUrls(value);
                }
            }
            return;
        }

        if (node instanceof ArrayNode arrayNode) {
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode value = arrayNode.get(i);
                if (value.isTextual()) {
                    arrayNode.set(i, objectMapper.getNodeFactory()
                            .textNode(aliOssUtil.generateAccessUrls(value.textValue())));
                } else {
                    replaceOssUrls(value);
                }
            }
        }
    }
}
