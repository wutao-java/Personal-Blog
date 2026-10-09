package cc.wutao.controller.admin;

import cc.wutao.annotation.RateLimit;
import cc.wutao.dto.AiCoverGenerateDTO;
import cc.wutao.result.Result;
import cc.wutao.service.article.AiCoverService;
import cc.wutao.vo.AiCoverStatusVO;
import cc.wutao.vo.AiCoverVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin/article-cover")
@RequiredArgsConstructor
public class AiCoverController {

    private final AiCoverService service;

    @GetMapping("/status")
    public Result<AiCoverStatusVO> status() {
        return Result.success(service.status());
    }

    @PostMapping
    @RateLimit(type = RateLimit.Type.IP, tokens = 3, burstCapacity = 3,
            timeWindow = 1, timeUnit = TimeUnit.MINUTES, message = "生图请求过于频繁，请稍后再试")
    public Result<AiCoverVO> generate(@Valid @RequestBody AiCoverGenerateDTO request) {
        return Result.success(service.generate(request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> discard(@PathVariable String id) {
        service.discard(id);
        return Result.success();
    }
}
