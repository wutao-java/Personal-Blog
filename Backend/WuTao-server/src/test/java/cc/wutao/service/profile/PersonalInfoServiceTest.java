package cc.wutao.service.profile;

import cc.wutao.dto.PersonalInfoDTO;
import cc.wutao.entity.PersonalInfo;
import cc.wutao.mapper.PersonalInfoMapper;
import cc.wutao.vo.PersonalInfoVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersonalInfoServiceTest {

    private PersonalInfoMapper personalInfoMapper;
    private PersonalInfoService personalInfoService;

    @BeforeEach
    void setUp() {
        personalInfoMapper = mock(PersonalInfoMapper.class);
        personalInfoService = new PersonalInfoService(personalInfoMapper);
    }

    @Test
    void getAllPersonalInfoReturnsEmptyObjectWhenNotConfigured() {
        when(personalInfoMapper.getPersonalInfo()).thenReturn(null);

        PersonalInfo result = personalInfoService.getAllPersonalInfo();

        assertNotNull(result);
        assertNull(result.getId());
    }

    @Test
    void getPersonalInfoReturnsEmptyObjectWhenNotConfigured() {
        when(personalInfoMapper.getPersonalInfo()).thenReturn(null);

        PersonalInfoVO result = personalInfoService.getPersonalInfo();

        assertNotNull(result);
        assertNull(result.getId());
    }

    @Test
    void updatePersonalInfoCreatesOrUpdatesSingletonRecord() {
        PersonalInfoDTO request = PersonalInfoDTO.builder()
                .nickname("tester")
                .tag("developer")
                .website("http://localhost:5175")
                .build();

        personalInfoService.updatePersonalInfo(request);

        ArgumentCaptor<PersonalInfo> captor = ArgumentCaptor.forClass(PersonalInfo.class);
        verify(personalInfoMapper).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals("http://localhost:5175", captor.getValue().getWebsite());
    }
}
