package cc.feitwnd.controller.blog;

import cc.feitwnd.entity.CityFootprint;
import cc.feitwnd.entity.CityImage;
import cc.feitwnd.result.Result;
import cc.feitwnd.service.city.CityFootprintService;
import cc.feitwnd.service.city.CityImageService;
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
