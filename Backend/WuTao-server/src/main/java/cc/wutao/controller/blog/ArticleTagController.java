package cc.wutao.controller.blog;

import cc.wutao.entity.ArticleTags;
import cc.wutao.result.PageResult;
import cc.wutao.result.Result;
import cc.wutao.service.article.ArticleService;
import cc.wutao.service.article.ArticleTagService;
import cc.wutao.vo.BlogArticleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 博客端文章标签接口
 */
@Slf4j
@RestController("blogArticleTagController")
@RequestMapping("/blog/article/tag")
@RequiredArgsConstructor
public class ArticleTagController {

    private final ArticleTagService articleTagService;

    private final ArticleService articleService;

    /**
     * 获取有已发布文章的标签列表
     */
    @GetMapping
    public Result<List<ArticleTags>> getVisibleTags() {
        List<ArticleTags> list = articleTagService.getVisibleTags();
        return Result.success(list);
    }

    /**
     * 根据标签ID获取已发布文章列表
     */
    @GetMapping("/{tagId}")
    public Result<PageResult<BlogArticleVO>> getPublishedByTagId(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("博客端根据标签获取文章列表: tagId={}", tagId);
        PageResult<BlogArticleVO> pageResult =
                articleService.getPublishedByTagId(tagId, page, pageSize);
        return Result.success(pageResult);
    }
}
