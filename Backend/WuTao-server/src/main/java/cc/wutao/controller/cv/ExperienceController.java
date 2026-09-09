package cc.wutao.controller.cv;

import cc.wutao.entity.Experiences;
import cc.wutao.result.Result;
import cc.wutao.service.profile.ExperienceService;
import cc.wutao.vo.ExperienceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 简历端经历接口
 */
@RestController("cvExperienceController")
@RequestMapping("/cv/experience")
@RequiredArgsConstructor
public class ExperienceController {

    private final ExperienceService experienceService;

    /**
     * 获取全部经历信息
     */
    @GetMapping
    public Result<List<ExperienceVO>> getAllExperience() {
        List<ExperienceVO> experienceList = experienceService.getAllExperience();
        return Result.success(experienceList);
    }
}
