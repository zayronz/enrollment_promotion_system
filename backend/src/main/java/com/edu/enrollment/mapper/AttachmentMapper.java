package com.edu.enrollment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.enrollment.entity.AttachmentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AttachmentMapper extends BaseMapper<AttachmentEntity> {

    @Select("SELECT * FROM attachment WHERE related_id = #{relatedId} AND related_type = #{relatedType}")
    List<AttachmentEntity> selectByRelated(@Param("relatedId") Long relatedId,
                                           @Param("relatedType") String relatedType);
}