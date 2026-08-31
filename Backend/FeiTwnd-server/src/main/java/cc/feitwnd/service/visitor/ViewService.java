package cc.feitwnd.service.visitor;

import cc.feitwnd.dto.ViewPageQueryDTO;
import cc.feitwnd.entity.Views;
import cc.feitwnd.mapper.ViewMapper;
import cc.feitwnd.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ViewService {

    private final ViewMapper viewMapper;

    /**
     * 分页查询浏览记录
     * @param viewPageQueryDTO
     * @return
     */
    public PageResult<Views> pageQuery(ViewPageQueryDTO viewPageQueryDTO) {
        PageHelper.startPage(viewPageQueryDTO.getPage(), viewPageQueryDTO.getPageSize());
        Page<Views> page = viewMapper.pageQuery(viewPageQueryDTO);
        long total = page.getTotal();
        List<Views> records = page.getResult();
        return new PageResult<>(total, records);
    }

    /**
     * 批量删除浏览记录
     * @param ids
     */
    public void batchDelete(List<Long> ids) {
        viewMapper.batchDelete(ids);
    }
}
