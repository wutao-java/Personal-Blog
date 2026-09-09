package cc.wutao.service.rss;

import cc.wutao.properties.WebsiteProperties;
import cc.wutao.result.PageResult;
import cc.wutao.service.article.ArticleService;
import cc.wutao.vo.BlogArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SitemapService {

    private final ArticleService articleService;
    private final WebsiteProperties websiteProperties;

    private static final DateTimeFormatter W3C_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 生成Sitemap XML
     * @return
     */
    public String generateSitemap() {
        String BLOG_BASE_URL = websiteProperties.getBlog();

        // 获取所有已发布文章（取足够多，最多500条）
        PageResult<BlogArticleVO> pageResult = articleService.getPublishedPage(1, 500);
        List<BlogArticleVO> articles = pageResult.getRecords();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // 首页
        xml.append("  <url>\n");
        xml.append("    <loc>").append(escapeXml(BLOG_BASE_URL)).append("</loc>\n");
        xml.append("    <changefreq>daily</changefreq>\n");
        xml.append("    <priority>1.0</priority>\n");
        xml.append("  </url>\n");

        // 文章页面
        if (articles != null) {
            for (BlogArticleVO article : articles) {
                xml.append("  <url>\n");
                xml.append("    <loc>").append(escapeXml(BLOG_BASE_URL + "/article/" + article.getSlug()))
                        .append("</loc>\n");
                if (article.getPublishTime() != null) {
                    xml.append("    <lastmod>").append(article.getPublishTime().format(W3C_DATE_FMT)).append("</lastmod>\n");
                }
                xml.append("    <changefreq>weekly</changefreq>\n");
                xml.append("    <priority>0.8</priority>\n");
                xml.append("  </url>\n");
            }
        }

        // 归档页
        xml.append("  <url>\n");
        xml.append("    <loc>").append(escapeXml(BLOG_BASE_URL + "/archive")).append("</loc>\n");
        xml.append("    <changefreq>weekly</changefreq>\n");
        xml.append("    <priority>0.6</priority>\n");
        xml.append("  </url>\n");

        // 友链页
        xml.append("  <url>\n");
        xml.append("    <loc>").append(escapeXml(BLOG_BASE_URL + "/friends")).append("</loc>\n");
        xml.append("    <changefreq>monthly</changefreq>\n");
        xml.append("    <priority>0.5</priority>\n");
        xml.append("  </url>\n");

        // 留言板
        xml.append("  <url>\n");
        xml.append("    <loc>").append(escapeXml(BLOG_BASE_URL + "/message")).append("</loc>\n");
        xml.append("    <changefreq>daily</changefreq>\n");
        xml.append("    <priority>0.5</priority>\n");
        xml.append("  </url>\n");

        xml.append("</urlset>\n");

        return xml.toString();
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
