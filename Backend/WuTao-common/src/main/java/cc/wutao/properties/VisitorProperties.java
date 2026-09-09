package cc.wutao.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wutao.visitor")
@Data
public class VisitorProperties {
    private String verifyCode;
}
