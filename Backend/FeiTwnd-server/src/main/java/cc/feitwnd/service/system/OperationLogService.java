package cc.feitwnd.service.system;

import cc.feitwnd.dto.OperationLogPageQueryDTO;
import cc.feitwnd.entity.OperationLogs;
import cc.feitwnd.mapper.OperationLogMapper;
import cc.feitwnd.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    /**
     * 保存操作日志
     * @param operationLogs
     */
    public void save(OperationLogs operationLogs) {
        operationLogMapper.save(operationLogs);
    }

    /**
     * 分页查询操作日志
     * @param operationLogPageQueryDTO
     * @return
     */
    public PageResult<OperationLogs> pageQuery(OperationLogPageQueryDTO operationLogPageQueryDTO) {
        PageHelper.startPage(operationLogPageQueryDTO.getPage(), operationLogPageQueryDTO.getPageSize());
        Page<OperationLogs> page = operationLogMapper.pageQuery(operationLogPageQueryDTO);
        long total = page.getTotal();
        List<OperationLogs> records = page.getResult();
        return new PageResult<>(total, records);
    }

    /**
     * 批量删除操作日志
     * @param ids
     */
    public void batchDelete(List<Long> ids) {
        operationLogMapper.batchDelete(ids);
    }
}
