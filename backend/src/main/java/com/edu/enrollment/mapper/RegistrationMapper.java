package com.edu.enrollment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.enrollment.entity.RegistrationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RegistrationMapper extends BaseMapper<RegistrationEntity> {

    @Select("SELECT * FROM registration WHERE activity_id = #{activityId} AND user_id = #{userId}")
    RegistrationEntity findByActivityAndUser(@Param("activityId") Long activityId,
                                             @Param("userId") Long userId);

    @Select("SELECT * FROM registration WHERE activity_id = #{activityId} AND target_school = #{school}")
    List<RegistrationEntity> findByActivityAndSchool(@Param("activityId") Long activityId,
                                                     @Param("school") String school);

    @Update("UPDATE registration SET group_name = #{groupName}, group_rank = #{groupRank} WHERE id = #{id}")
    void updateGroupInfo(@Param("id") Long id,
                         @Param("groupName") String groupName,
                         @Param("groupRank") Integer groupRank);
}