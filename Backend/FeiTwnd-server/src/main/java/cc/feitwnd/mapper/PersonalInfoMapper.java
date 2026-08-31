package cc.feitwnd.mapper;

import cc.feitwnd.annotation.AutoFill;
import cc.feitwnd.entity.PersonalInfo;
import cc.feitwnd.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PersonalInfoMapper {

    /**
     * 获取个人信息
     */
    @Select("select * from personal_info where id = 1")
    PersonalInfo getPersonalInfo();

    /**
     * 保存个人信息
     */
    @AutoFill(value = OperationType.INSERT)
    void save(PersonalInfo personalInfo);
}
