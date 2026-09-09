package cc.wutao.controller.admin;

import cc.wutao.annotation.OperationLog;
import cc.wutao.dto.PersonalInfoDTO;
import cc.wutao.entity.PersonalInfo;
import cc.wutao.enumeration.OperationType;
import cc.wutao.result.Result;
import cc.wutao.service.profile.PersonalInfoService;
import cc.wutao.vo.PersonalInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端个人信息接口
 */
@RestController("adminPersonalInfoController")
@RequestMapping("/admin/personalInfo")
@Slf4j
@RequiredArgsConstructor
public class PersonalInfoController {

    private final PersonalInfoService personalInfoService;

    /**
     * 获取个人信息
     */
    @GetMapping
    public Result<PersonalInfo> getPersonalInfo() {
        PersonalInfo personalInfo = personalInfoService.getAllPersonalInfo();
        return Result.success(personalInfo);
    }

    /**
     * 更新个人信息
     */
    @PutMapping
    @OperationLog(value = OperationType.UPDATE, target = "personalInfo", targetId = "#personalInfoDTO.id")
    public Result updatePersonalInfo(@Valid @RequestBody PersonalInfoDTO personalInfoDTO) {
        log.info("更新个人信息: {}", personalInfoDTO);
        personalInfoService.updatePersonalInfo(personalInfoDTO);
        return Result.success();
    }
}
