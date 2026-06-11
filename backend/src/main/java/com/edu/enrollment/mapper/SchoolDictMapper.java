package com.edu.enrollment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.enrollment.entity.SchoolDictEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SchoolDictMapper extends BaseMapper<SchoolDictEntity> {

    @Select("SELECT * FROM school_dict WHERE standard_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR alias_names LIKE CONCAT('%', #{keyword}, '%') ORDER BY create_time DESC LIMIT 20")
    List<SchoolDictEntity> searchByName(@Param("keyword") String keyword);
}