package cc.wutao.controller.blog;

import cc.wutao.entity.CityFootprint;
import cc.wutao.entity.CityImage;
import cc.wutao.result.Result;
import cc.wutao.service.city.CityFootprintService;
import cc.wutao.service.city.CityImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 博客端城市足迹接口
 */
@RestController("blogCityFootprintController")
@RequestMapping("/blog/footprint")
@RequiredArgsConstructor
public class CityFootprintController {

    private final CityFootprintService cityFootprintService;

    private final CityImageService cityImageService;

    /**
     * 获取可见城市足迹
     */
    @GetMapping
    public Result<List<CityFootprint>> getVisibleFootprints() {
        List<CityFootprint> list = cityFootprintService.getVisible();
        return Result.success(list);
    }

    /**
     * 获取城市图片
     */
    @GetMapping("/image")
    public Result<List<CityImage>> getCityImages(@RequestParam Long cityId) {
        List<CityImage> list = cityImageService.getByCityId(cityId);
        return Result.success(list);
    }
}
