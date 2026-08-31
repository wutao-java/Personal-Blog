package cc.feitwnd.service.rss;

import cc.feitwnd.properties.WebsiteProperties;
import cc.feitwnd.result.PageResult;
import cc.feitwnd.service.article.ArticleService;
import cc.feitwnd.service.profile.PersonalInfoService;
import cc.feitwnd.vo.BlogArticleVO;
import cc.feitwnd.vo.PersonalInfoVO;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class XmlFeedServiceTest {

    @Test
    void generatedFeedsEscapeUrlsAndSlugs() {
        ArticleService articleService = mock(ArticleService.class);
        WebsiteProperties websiteProperties = mock(WebsiteProperties.class);
        PersonalInfoService personalInfoService = mock(PersonalInfoService.class);
        BlogArticleVO article = BlogArticleVO.builder()
                .title("title")
                .slug("java&spring")
                .build();
        when(articleService.getPublishedPage(1, 20))
                .thenReturn(new PageResult<>(1, List.of(article)));
        when(articleService.getPublishedPage(1, 500))
                .thenReturn(new PageResult<>(1, List.of(article)));
        when(articleService.getContentsBySlugs(List.of("java&spring"))).thenReturn(List.of());
        when(websiteProperties.getBlog()).thenReturn("https://example.com/blog?a=1&b=2");
        when(personalInfoService.getPersonalInfo())
                .thenReturn(PersonalInfoVO.builder().nickname("author").build());

        RssFeedService rssFeedService = new RssFeedService(
                articleService, personalInfoService, websiteProperties);
        SitemapService sitemapService = new SitemapService(articleService, websiteProperties);

        assertAll(
                () -> assertDoesNotThrow(() -> parse(rssFeedService.generateRssFeed())),
                () -> assertDoesNotThrow(() -> parse(sitemapService.generateSitemap()))
        );
    }

    private void parse(String xml) throws Exception {
        DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
    }
}
