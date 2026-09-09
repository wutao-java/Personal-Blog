package cc.wutao.controller.admin;

import cc.wutao.annotation.OperationLog;
import cc.wutao.dto.SocialMediaDTO;
import cc.wutao.entity.SocialMedia;
import cc.wutao.enumeration.OperationType;
import cc.wutao.result.Result;
import cc.wutao.service.profile.SocialMediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  管理端社交媒体接口
 */
@RestController("adminSocialMediaController")
@RequestMapping("/admin/socialMedia")
@Slf4j
@RequiredArgsConstructor
public class SocialMediaController {

    private final SocialMediaService socialMediaService;

    /**
     * 获取所有社交媒体信息
     */
    @GetMapping
    public Result<List<SocialMedia>> getAllSocialMedia() {
        List<SocialMedia> socialMediaList = socialMediaService.getAllSocialMedia();
        return Result.success(socialMediaList);
    }

    /**
     * 添加社交媒体信息
     */
    @PostMapping
    @OperationLog(value = OperationType.INSERT, target = "socialMedia")
    public Result addSocialMedia(@Valid @RequestBody SocialMediaDTO socialMediaDTO) {
        log.info("添加社交媒体信息: {}", socialMediaDTO);
        socialMediaService.addSocialMedia(socialMediaDTO);
        return Result.success();
    }
    /**
     * 批量删除社交媒体信息
     */
    @DeleteMapping
    @OperationLog(value = OperationType.DELETE, target = "socialMedia", targetId = "#ids")
    public Result deleteSocialMedia(@RequestParam List<Long> ids) {
        log.info("批量删除社交媒体信息: {}", ids);
        socialMediaService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 修改社交媒体信息
     */
    @PutMapping
    @OperationLog(value = OperationType.UPDATE, target = "socialMedia", targetId = "#socialMediaDTO.id")
    public Result updateSocialMedia(@Valid @RequestBody SocialMediaDTO socialMediaDTO) {
        log.info("修改社交媒体信息: {}", socialMediaDTO);
        socialMediaService.updateSocialMedia(socialMediaDTO);
        return Result.success();
    }
}
