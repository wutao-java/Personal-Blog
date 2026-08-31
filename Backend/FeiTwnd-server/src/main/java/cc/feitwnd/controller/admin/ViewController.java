package cc.feitwnd.controller.admin;

import cc.feitwnd.dto.ViewPageQueryDTO;
import cc.feitwnd.entity.Views;
import cc.feitwnd.result.PageResult;
import cc.feitwnd.result.Result;
import cc.feitwnd.service.visitor.ViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端浏览记录接口
 */
@Slf4j
@RestController("adminViewController")
@RequestMapping("/admin/view")
@RequiredArgsConstructor
public class ViewController {

    private final ViewService viewService;

    /**
     * 获取浏览记录列表
     * @param viewPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult<Views>> getViewList(ViewPageQueryDTO viewPageQueryDTO) {
        log.info("获取浏览记录列表,{}", viewPageQueryDTO);
        PageResult<Views> pageResult = viewService.pageQuery(viewPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除浏览记录
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result batchDelete(@RequestParam List<Long> ids) {
        log.info("批量删除浏览记录,{}", ids);
        viewService.batchDelete(ids);
        return Result.success();
    }
}
