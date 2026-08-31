package cc.feitwnd.service.message;

import cc.feitwnd.dto.MessageEditDTO;
import cc.feitwnd.entity.Messages;
import cc.feitwnd.exception.ValidationException;
import cc.feitwnd.mapper.MessageMapper;
import cc.feitwnd.properties.EmailProperties;
import cc.feitwnd.properties.WebsiteProperties;
import cc.feitwnd.service.auth.CaptchaService;
import cc.feitwnd.service.email.AsyncEmailService;
import cc.feitwnd.service.visitor.UserAgentService;
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
