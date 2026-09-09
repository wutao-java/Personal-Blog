package cc.wutao.controller.home;

import cc.wutao.result.Result;
import cc.wutao.service.profile.PersonalInfoService;
import cc.wutao.vo.PersonalInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  首页端个人信息接口
 */
@RestController("homePersonalInfoController")
@RequestMapping("/home/personalInfo")
@RequiredArgsConstructor
public class PersonalInfoController {

    private final PersonalInfoService personalInfoService;

    /**
     * 获取个人信息
     */
    @GetMapping
    public Result<PersonalInfoVO> getPersonalInfo() {
        PersonalInfoVO personalInfoVO = personalInfoService.getPersonalInfo();
        return Result.success(personalInfoVO);
    }
}
