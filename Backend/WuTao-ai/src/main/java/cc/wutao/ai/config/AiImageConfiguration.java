package cc.wutao.ai.config;

import cc.wutao.ai.properties.AiImageProperties;
import cc.wutao.ai.service.CompatibleAiCoverGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "wutao.ai.image", name = "enabled", havingValue = "true")
public class AiImageConfiguration {

    @Bean
    public CompatibleAiCoverGenerator aiCoverGenerator(AiImageProperties properties, ObjectMapper objectMapper) {
        return new CompatibleAiCoverGenerator(properties, objectMapper);
    }
}
