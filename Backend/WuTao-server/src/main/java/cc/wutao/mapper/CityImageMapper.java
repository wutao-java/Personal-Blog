package cc.wutao.mapper;

import cc.wutao.annotation.AutoFill;
import cc.wutao.entity.CityImage;
import cc.wutao.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CityImageMapper {

    /**
     * 获取某城市的所有图片
     */
    @Select("select * from city_images where city_id = #{cityId} order by sort asc, id asc")
    List<CityImage> getByCityId(Long cityId);

    /**
     * 添加城市图片
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(CityImage cityImage);

    /**
     * 修改城市图片
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(CityImage cityImage);

    /**
     * 删除单张图片
     */
    @Delete("delete from city_images where id = #{id}")
    void delete(Long id);

    /**
     * 根据城市ID删除所有图片
     */
    @Delete("delete from city_images where city_id = #{cityId}")
    void batchDeleteByCityId(Long cityId);
}
