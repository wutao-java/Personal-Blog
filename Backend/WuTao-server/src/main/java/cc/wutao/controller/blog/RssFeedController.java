package cc.wutao.controller.blog;

import cc.wutao.properties.WebsiteProperties;
import cc.wutao.result.Result;
import cc.wutao.service.article.ArticleService;
import cc.wutao.service.profile.PersonalInfoService;
import cc.wutao.service.rss.RssFeedService;
import cc.wutao.vo.BlogArticleVO;
import cc.wutao.vo.PersonalInfoVO;
import cc.wutao.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 博客端 RSS Feed 接口
 */
@Slf4j
@RestController("blogRssFeedController")
@RequestMapping("/blog")
@RequiredArgsConstructor
public class RssFeedController {

    private final RssFeedService rssFeedService;

    /**
     * 生成 RSS 2.0 Feed XML
     */
    @GetMapping(value = "/rss", produces = "application/xml; charset=UTF-8")
    @Cacheable(value = "rssFeed", key = "'xml'")
    public String rssFeed() {
        String xml = rssFeedService.generateRssFeed();
        return xml;
    }
}
