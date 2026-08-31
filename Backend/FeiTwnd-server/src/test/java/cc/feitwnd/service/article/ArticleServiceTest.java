package cc.feitwnd.service.article;

import cc.feitwnd.dto.ArticleDTO;
import cc.feitwnd.entity.Articles;
import cc.feitwnd.mapper.ArticleMapper;
import cc.feitwnd.mapper.ArticleTagMapper;
import cc.feitwnd.service.async.SummaryBackfillAsyncService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ArticleServiceTest {

    private ArticleMapper articleMapper;
    private SummaryBackfillAsyncService summaryBackfillAsyncService;
    private ArticleService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        articleMapper = mock(ArticleMapper.class);
        summaryBackfillAsyncService = mock(SummaryBackfillAsyncService.class);

        doAnswer(invocation -> {
            invocation.<Articles>getArgument(0).setId(1L);
            return null;
        }).when(articleMapper).insert(any(Articles.class));
        service = new ArticleService(
                articleMapper,
                mock(ArticleTagMapper.class),
                mock(RedisTemplate.class),
                summaryBackfillAsyncService
        );
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void publishSideEffectsRunOnlyAfterTransactionCommit() {
        ArticleDTO article = ArticleDTO.builder()
                .title("title")
                .slug("slug")
                .summary(" ")
                .contentMarkdown("content")
                .contentHtml("<p>content</p>")
                .categoryId(1L)
                .isPublished(1)
                .aiGenerateSummary(true)
                .build();

        service.createArticle(article);

        verifyNoInteractions(summaryBackfillAsyncService);

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

        verify(summaryBackfillAsyncService).backfillSummaryAsync(1L, "title", "content");
    }
}
