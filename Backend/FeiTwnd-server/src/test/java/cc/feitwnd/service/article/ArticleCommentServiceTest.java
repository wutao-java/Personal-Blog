package cc.feitwnd.service.article;

import cc.feitwnd.dto.ArticleCommentEditDTO;
import cc.feitwnd.dto.ArticleCommentReplyDTO;
import cc.feitwnd.entity.ArticleComments;
import cc.feitwnd.exception.ValidationException;
import cc.feitwnd.mapper.ArticleCommentMapper;
import cc.feitwnd.mapper.ArticleMapper;
import cc.feitwnd.properties.EmailProperties;
import cc.feitwnd.properties.WebsiteProperties;
import cc.feitwnd.service.auth.CaptchaService;
import cc.feitwnd.service.email.AsyncEmailService;
import cc.feitwnd.service.visitor.UserAgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ArticleCommentServiceTest {

    private ArticleCommentMapper commentMapper;
    private AsyncEmailService asyncEmailService;
    private ArticleCommentService service;

    @BeforeEach
    void setUp() {
        commentMapper = mock(ArticleCommentMapper.class);
        asyncEmailService = mock(AsyncEmailService.class);
        WebsiteProperties websiteProperties = mock(WebsiteProperties.class);
        when(websiteProperties.getTitle()).thenReturn("admin");
        service = new ArticleCommentService(
                commentMapper,
                mock(ArticleMapper.class),
                mock(UserAgentService.class),
                asyncEmailService,
                websiteProperties,
                mock(EmailProperties.class),
                mock(CaptchaService.class)
        );
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void deletingSelectedRootAndChildDoesNotDoubleCountChild() {
        ArticleComments root = ArticleComments.builder()
                .id(1L)
                .articleId(10L)
                .isApproved(1)
                .build();
        ArticleComments child = ArticleComments.builder()
                .id(2L)
                .articleId(10L)
                .rootId(1L)
                .isApproved(1)
                .build();
        when(commentMapper.getByIds(List.of(1L, 2L))).thenReturn(List.of(root, child));
        when(commentMapper.countApprovedByRootId(1L)).thenReturn(1);

        service.batchDelete(List.of(1L, 2L));

        verify(commentMapper).subtractCommentCount(10L, 2);
        verify(commentMapper).deleteByRootId(1L);
        verify(commentMapper).batchDelete(List.of(1L, 2L));
    }

    @Test
    void adminReplyCannotBeEditedAsVisitor() {
        ArticleComments adminReply = ArticleComments.builder().id(1L).visitorId(null).build();
        when(commentMapper.getById(1L)).thenReturn(adminReply);
        ArticleCommentEditDTO edit = ArticleCommentEditDTO.builder()
                .id(1L)
                .visitorId(9L)
                .content("updated")
                .build();

        assertThrows(ValidationException.class, () -> service.editComment(edit));
    }

    @Test
    void replyNotificationRunsOnlyAfterTransactionCommit() {
        ArticleComments parent = ArticleComments.builder()
                .id(1L)
                .nickname("reader")
                .content("parent")
                .emailOrQq("reader@example.com")
                .isNotice(1)
                .isAdminReply(0)
                .build();
        when(commentMapper.getById(1L)).thenReturn(parent);
        ArticleCommentReplyDTO reply = ArticleCommentReplyDTO.builder()
                .articleId(10L)
                .parentId(1L)
                .content("reply")
                .build();

        service.adminReply(reply, null);

        verifyNoInteractions(asyncEmailService);
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);
        verify(asyncEmailService).sendReplyNotificationAsync(
                "reader@example.com", "reader", "parent", "admin", "reply", "comment");
    }
}
