package cc.wutao.service.article;

import cc.wutao.dto.ArticleDTO;
import cc.wutao.entity.Articles;
import cc.wutao.mapper.ArticleMapper;
import cc.wutao.mapper.ArticleTagMapper;
import cc.wutao.service.async.SummaryBackfillAsyncService;
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
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

class ArticleServiceTest {

    private ArticleMapper articleMapper;
    private SummaryBackfillAsyncService summaryBackfillAsyncService;
    private ArticleService service;
    private AiCoverService aiCoverService;
    private AiCoverCleanupService cleanupService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        articleMapper = mock(ArticleMapper.class);
        summaryBackfillAsyncService = mock(SummaryBackfillAsyncService.class);
        aiCoverService = mock(AiCoverService.class);
        cleanupService = mock(AiCoverCleanupService.class);

        doAnswer(invocation -> {
            invocation.<Articles>getArgument(0).setId(1L);
            return null;
        }).when(articleMapper).insert(any(Articles.class));
        service = new ArticleService(
                articleMapper,
                mock(ArticleTagMapper.class),
                mock(RedisTemplate.class),
                summaryBackfillAsyncService,
                aiCoverService,
                cleanupService
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

    @Test
    void savingDraftBindsConfirmedCoverAndReturnsCreatedIdWithoutGeneration() {
        ArticleDTO article = ArticleDTO.builder().title("title").slug("slug")
                .contentMarkdown("content").categoryId(1L).isPublished(0)
                .coverImage("https://cover").aiCoverId("candidate").build();

        assertEquals(1L, service.createArticle(article));
        verify(aiCoverService).bindCover(1L, "https://cover", "candidate");
        verifyNoInteractions(summaryBackfillAsyncService);
        verifyNoInteractions(cleanupService);
    }

    @Test
    void deleteLocksArticlesAndQueuesOnlyTrackedAiAssetsBeforeCommit() {
        when(articleMapper.getByIdForUpdate(1L)).thenReturn(Articles.builder().id(1L).build());

        service.batchDelete(List.of(1L));

        verify(articleMapper).getByIdForUpdate(1L);
        verify(aiCoverService).markArticleDeleted(1L);
        verify(articleMapper).batchDelete(List.of(1L));
        verifyNoInteractions(cleanupService);
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);
        verify(cleanupService).cleanupAsync();
    }

    @Test
    void deleteRollbackDoesNotRequestOssCleanup() {
        when(articleMapper.getByIdForUpdate(1L)).thenReturn(Articles.builder().id(1L).build());

        service.batchDelete(List.of(1L));
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(synchronization -> synchronization.afterCompletion(
                        TransactionSynchronization.STATUS_ROLLED_BACK));

        verifyNoInteractions(cleanupService);
    }
}
