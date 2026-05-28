package com.edu.enrollment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.enrollment.entity.FeedbackEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FeedbackMapper extends BaseMapper<FeedbackEntity> {

    @Select("SELECT * FROM feedback WHERE activity_id = #{activityId} ORDER BY create_time DESC")
    List<FeedbackEntity> selectByActivityId(Long activityId);

    @Select("SELECT * FROM feedback WHERE activity_id = #{activityId} AND user_id = #{userId}")
    List<FeedbackEntity> selectByActivityAndUser(@Param("activityId") Long activityId,
                                                 @Param("userId") Long userId);
}