package cc.wutao.service.message;

import cc.wutao.dto.MessageEditDTO;
import cc.wutao.entity.Messages;
import cc.wutao.exception.ValidationException;
import cc.wutao.mapper.MessageMapper;
import cc.wutao.properties.EmailProperties;
import cc.wutao.properties.WebsiteProperties;
import cc.wutao.service.auth.CaptchaService;
import cc.wutao.service.email.AsyncEmailService;
import cc.wutao.service.visitor.UserAgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class MessageServiceTest {

    private MessageMapper messageMapper;
    private MessageService service;

    @BeforeEach
    void setUp() {
        messageMapper = mock(MessageMapper.class);
        service = new MessageService(
                messageMapper,
                mock(UserAgentService.class),
                mock(AsyncEmailService.class),
                mock(WebsiteProperties.class),
                mock(EmailProperties.class),
                mock(CaptchaService.class)
        );
    }

    @Test
    void adminReplyCannotBeEditedAsVisitor() {
        Messages adminReply = Messages.builder().id(1L).visitorId(null).build();
        when(messageMapper.getById(1L)).thenReturn(adminReply);
        MessageEditDTO edit = MessageEditDTO.builder()
                .id(1L)
                .visitorId(9L)
                .content("updated")
                .build();

        assertThrows(ValidationException.class, () -> service.editMessage(edit));
    }

    @Test
    void batchDeleteUsesSingleMapperOperation() {
        List<Long> ids = List.of(1L, 2L);

        service.batchDelete(ids);

        verify(messageMapper).batchDelete(ids);
        verifyNoMoreInteractions(messageMapper);
    }
}
