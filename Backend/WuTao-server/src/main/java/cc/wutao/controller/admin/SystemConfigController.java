package cc.wutao.controller.admin;

import cc.wutao.annotation.OperationLog;
import cc.wutao.dto.SystemConfigDTO;
import cc.wutao.entity.SystemConfig;
import cc.wutao.enumeration.OperationType;
import cc.wutao.result.Result;
import cc.wutao.service.system.SystemConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端系统配置接口
 */
@Slf4j
@RestController("adminSystemConfigController")
@RequestMapping("/admin/systemConfig")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * 获取所有系统配置
     * @return
     */
    @GetMapping
    public Result<List<SystemConfig>> listAll() {
        List<SystemConfig> configList = systemConfigService.listAll();
        return Result.success(configList);
    }

    /**
     * 根据配置键获取配置
     * @param configKey
     * @return
     */
    @GetMapping("/key/{configKey}")
    public Result<SystemConfig> getByKey(@PathVariable String configKey) {
        log.info("根据配置键获取配置,{}", configKey);
        SystemConfig systemConfig = systemConfigService.getByKey(configKey);
        return Result.success(systemConfig);
    }

    /**
     * 根据ID获取配置
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SystemConfig> getById(@PathVariable Long id) {
        log.info("根据ID获取配置,{}", id);
        SystemConfig systemConfig = systemConfigService.getById(id);
        return Result.success(systemConfig);
    }

    /**
     * 添加系统配置
     * @param systemConfigDTO
     * @return
     */
    @PostMapping
    @OperationLog(value = OperationType.INSERT, target = "systemConfig")
    public Result addConfig(@Valid @RequestBody SystemConfigDTO systemConfigDTO) {
        log.info("添加系统配置,{}", systemConfigDTO);
        systemConfigService.addConfig(systemConfigDTO);
        return Result.success();
    }

    /**
     * 更新系统配置
     * @param systemConfigDTO
     * @return
     */
    @PutMapping
    @OperationLog(value = OperationType.UPDATE, target = "systemConfig", targetId = "#systemConfigDTO.id")
    public Result updateConfig(@Valid @RequestBody SystemConfigDTO systemConfigDTO) {
        log.info("更新系统配置,{}", systemConfigDTO);
        systemConfigService.updateConfig(systemConfigDTO);
        return Result.success();
    }

    /**
     * 批量删除系统配置
     * @param ids
     * @return
     */
    @DeleteMapping
    @OperationLog(value = OperationType.DELETE, target = "systemConfig", targetId = "#ids")
    public Result deleteConfig(@RequestParam List<Long> ids) {
        log.info("批量删除系统配置,{}", ids);
        systemConfigService.batchDelete(ids);
        return Result.success();
    }
}
