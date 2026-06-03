package com.edu.enrollment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.enrollment.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    @Select("SELECT * FROM user WHERE username = #{username}")
    UserEntity findByUsername(String username);

    @Update("UPDATE user SET avatar = #{avatar} WHERE id = #{userId}")
    int updateAvatar(@Param("userId") Long userId, @Param("avatar") String avatar);

    @Select("SELECT * FROM user WHERE id = #{id}")
    UserEntity selectUserById(Long id);
}
