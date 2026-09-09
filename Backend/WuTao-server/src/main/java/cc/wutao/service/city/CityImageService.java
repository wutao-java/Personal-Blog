package cc.wutao.service.city;

import cc.wutao.dto.CityImageDTO;
import cc.wutao.entity.CityImage;
import cc.wutao.mapper.CityImageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CityImageService {

    private final CityImageMapper cityImageMapper;

    public List<CityImage> getByCityId(Long cityId) {
        List<CityImage> list = cityImageMapper.getByCityId(cityId);
        return list != null && !list.isEmpty() ? list : Collections.emptyList();
    }

    public void add(CityImageDTO cityImageDTO) {
        CityImage cityImage = new CityImage();
        BeanUtils.copyProperties(cityImageDTO, cityImage);
        cityImageMapper.insert(cityImage);
    }

    public void update(CityImageDTO cityImageDTO) {
        CityImage cityImage = new CityImage();
        BeanUtils.copyProperties(cityImageDTO, cityImage);
        cityImageMapper.update(cityImage);
    }

    public void delete(Long id) {
        cityImageMapper.delete(id);
    }
}
