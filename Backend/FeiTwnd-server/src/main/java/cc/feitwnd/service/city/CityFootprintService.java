package cc.feitwnd.service.city;

import cc.feitwnd.dto.CityFootprintDTO;
import cc.feitwnd.dto.CityFootprintPageQueryDTO;
import cc.feitwnd.entity.CityFootprint;
import cc.feitwnd.mapper.CityFootprintMapper;
import cc.feitwnd.mapper.CityImageMapper;
import cc.feitwnd.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CityFootprintService {

    private final CityFootprintMapper cityFootprintMapper;

    private final CityImageMapper cityImageMapper;

    public PageResult<CityFootprint> pageQuery(CityFootprintPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<CityFootprint> page = cityFootprintMapper.pageQuery(pageQueryDTO);
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    public void add(CityFootprintDTO cityFootprintDTO) {
        CityFootprint cityFootprint = new CityFootprint();
        BeanUtils.copyProperties(cityFootprintDTO, cityFootprint);
        cityFootprintMapper.insert(cityFootprint);
    }

    public void update(CityFootprintDTO cityFootprintDTO) {
        CityFootprint cityFootprint = new CityFootprint();
        BeanUtils.copyProperties(cityFootprintDTO, cityFootprint);
        cityFootprintMapper.update(cityFootprint);
    }

    @Transactional
    public void batchDelete(List<Long> ids) {
        for (Long id : ids) {
            cityImageMapper.batchDeleteByCityId(id);
        }
        cityFootprintMapper.batchDelete(ids);
    }

    public List<CityFootprint> getVisible() {
        return cityFootprintMapper.getVisible();
    }
}
