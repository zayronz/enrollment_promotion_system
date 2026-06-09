package com.edu.enrollment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.enrollment.entity.SchoolDictEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SchoolDictMapper extends BaseMapper<SchoolDictEntity> {

    @Select("SELECT * FROM school_dict WHERE standard_name LIKE CONCAT('%', #{keyword}, '%') ORDER BY standard_name LIMIT 10")
    List<SchoolDictEntity> searchByName(@Param("keyword") String keyword);
}