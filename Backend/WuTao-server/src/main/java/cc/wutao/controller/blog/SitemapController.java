package cc.wutao.controller.blog;

import cc.wutao.properties.WebsiteProperties;
import cc.wutao.result.PageResult;
import cc.wutao.service.article.ArticleService;
import cc.wutao.service.rss.SitemapService;
import cc.wutao.vo.BlogArticleVO;
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
 * 博客端 Sitemap 接口
 */
@Slf4j
@RestController("blogSitemapController")
@RequestMapping("/blog")
@RequiredArgsConstructor
public class SitemapController {

    private final SitemapService sitemapService;


    /**
     * 动态生成站点地图 XML
     */
    @GetMapping(value = "/sitemap.xml", produces = "application/xml; charset=UTF-8")
    @Cacheable(value = "sitemap", key = "'xml'")
    public String sitemap() {
        String xml = sitemapService.generateSitemap();
        return xml;
    }
}
