package com.edu.enrollment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.enrollment.entity.ActivityEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ActivityMapper extends BaseMapper<ActivityEntity> {

    @Select("SELECT * FROM activity WHERE status = 1 ORDER BY activity_start_time DESC")
    List<ActivityEntity> selectOpenActivities();

    @Select("SELECT * FROM activity WHERE creator_id = #{creatorId} ORDER BY create_time DESC")
    List<ActivityEntity> selectByCreator(@Param("creatorId") Long creatorId);
}