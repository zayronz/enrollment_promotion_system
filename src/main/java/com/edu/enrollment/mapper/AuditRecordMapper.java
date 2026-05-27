package com.edu.enrollment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.enrollment.entity.AuditRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecordEntity> {

    @Select("SELECT * FROM audit_record WHERE registration_id = #{registrationId} ORDER BY create_time DESC")
    List<AuditRecordEntity> selectByRegistrationId(Long registrationId);
}