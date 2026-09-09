package cc.wutao.controller.cv;

import cc.wutao.entity.Skills;
import cc.wutao.result.Result;
import cc.wutao.service.profile.SkillService;
import cc.wutao.vo.SkillVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 简历端技能接口
 */
@RestController("cvSkillController")
@RequestMapping("/cv/skill")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    /**
     * 获取技能信息
     */
    @GetMapping
    public Result<List<SkillVO>> getSkill() {
        List<SkillVO> skillList = skillService.getSkillVO();
        return Result.success(skillList);
    }
}
