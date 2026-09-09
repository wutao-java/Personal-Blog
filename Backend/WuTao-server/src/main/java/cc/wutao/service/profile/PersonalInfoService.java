package cc.wutao.service.profile;


import cc.wutao.dto.PersonalInfoDTO;
import cc.wutao.entity.PersonalInfo;
import cc.wutao.mapper.PersonalInfoMapper;
import cc.wutao.vo.PersonalInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonalInfoService {

    private final PersonalInfoMapper personalInfoMapper;

    /**
     * 管理端获取所有个人信息
     * @return
     */
    @Cacheable(value = "personalInfo", key = "'all'")
    public PersonalInfo getAllPersonalInfo() {
        PersonalInfo personalInfo = personalInfoMapper.getPersonalInfo();
        return personalInfo == null ? new PersonalInfo() : personalInfo;
    }

    /**
     * 管理端更新个人信息
     * @param personalInfoDTO
     */
    @CacheEvict(value = "personalInfo", allEntries = true)
    public void updatePersonalInfo(PersonalInfoDTO personalInfoDTO) {
        PersonalInfo personalInfo = new PersonalInfo();
        BeanUtils.copyProperties(personalInfoDTO, personalInfo);
        personalInfo.setId(1L);
        personalInfoMapper.save(personalInfo);
    }

    /**
     * 其他端获取个人信息
     * @return
     */
    @Cacheable(value = "personalInfo", key = "'vo:v3'")
    public PersonalInfoVO getPersonalInfo() {
        PersonalInfo personalInfo = personalInfoMapper.getPersonalInfo();
        if (personalInfo == null) {
            return new PersonalInfoVO();
        }
        PersonalInfoVO personalInfoVO = PersonalInfoVO.builder()
                .id(personalInfo.getId())
                .nickname(personalInfo.getNickname())
                .tag(personalInfo.getTag())
                .description(personalInfo.getDescription())
                .avatar(personalInfo.getAvatar())
                .website(personalInfo.getWebsite())
                .email(personalInfo.getEmail())
                .github(personalInfo.getGithub())
                .location(personalInfo.getLocation())
                .build();
        return personalInfoVO;
    }
}
