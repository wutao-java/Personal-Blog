package cc.feitwnd.service.article;

import cc.feitwnd.mapper.ArticleLikeMapper;
import cc.feitwnd.mapper.ArticleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArticleLikeServiceTest {

    private ArticleLikeMapper likeMapper;
    private ArticleMapper articleMapper;
    private ArticleLikeService service;

    @BeforeEach
    void setUp() {
        likeMapper = mock(ArticleLikeMapper.class);
        articleMapper = mock(ArticleMapper.class);
        service = new ArticleLikeService(likeMapper, articleMapper);
    }

    @Test
    void concurrentDuplicateLikeRemainsIdempotent() {
        when(likeMapper.countByArticleIdAndVisitorId(10L, 9L)).thenReturn(0);
        doThrow(new DuplicateKeyException("duplicate"))
                .when(likeMapper).insert(any());

        assertDoesNotThrow(() -> service.likeArticle(10L, 9L));
        verify(articleMapper, never()).incrementLikeCount(10L);
    }

    @Test
    void concurrentMissingUnlikeDoesNotDecrementCount() {
        when(likeMapper.countByArticleIdAndVisitorId(10L, 9L)).thenReturn(1);
        when(likeMapper.deleteByArticleIdAndVisitorId(10L, 9L)).thenReturn(0);

        service.unlikeArticle(10L, 9L);

        verify(articleMapper, never()).decrementLikeCount(10L);
    }
}
