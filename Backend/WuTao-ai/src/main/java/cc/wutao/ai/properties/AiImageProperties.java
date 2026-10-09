package cc.wutao.ai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wutao.ai.image")
public class AiImageProperties {

    private boolean enabled = false;
    private String endpoint;
    private String apiKey;
    private String modelName;
    private String size = "1536x1024";
    // Leave empty for models that always return base64 and reject response_format.
    private String responseFormat;
    private int timeoutSeconds = 180;
}
