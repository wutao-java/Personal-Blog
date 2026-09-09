package cc.wutao.service.article;

import cc.wutao.entity.ArticleLikes;
import cc.wutao.mapper.ArticleLikeMapper;
import cc.wutao.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleLikeService {

    private final ArticleLikeMapper articleLikeMapper;

    private final ArticleMapper articleMapper;

    @Transactional
    public void likeArticle(Long articleId, Long visitorId) {
        ArticleLikes articleLikes = ArticleLikes.builder()
                .articleId(articleId)
                .visitorId(visitorId)
                .likeTime(LocalDateTime.now())
                .build();
        try {
            articleLikeMapper.insert(articleLikes);
        } catch (DuplicateKeyException e) {
            return;
        }
        // 文章点赞数+1
        articleMapper.incrementLikeCount(articleId);
    }

    @Transactional
    public void unlikeArticle(Long articleId, Long visitorId) {
        int deleted = articleLikeMapper.deleteByArticleIdAndVisitorId(articleId, visitorId);
        if (deleted > 0) {
            articleMapper.decrementLikeCount(articleId);
        }
    }

    public boolean hasLiked(Long articleId, Long visitorId) {
        return articleLikeMapper.countByArticleIdAndVisitorId(articleId, visitorId) > 0;
    }
}
