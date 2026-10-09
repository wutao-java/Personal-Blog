package cc.wutao.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wutao.website")
@Data
public class WebsiteProperties {
    // 网站标题
    private String title;
    // 管理端地址
    private String admin;
    // 简历地址
    private String cv;
    // 博客地址
    private String blog;
}
