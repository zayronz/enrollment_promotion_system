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

    @Select("SELECT * FROM user WHERE email = #{account} OR phone = #{account} LIMIT 1")
    UserEntity findByEmailOrPhone(@Param("account") String account);

    @Update("UPDATE user SET avatar = #{avatar} WHERE id = #{userId}")
    int updateAvatar(@Param("userId") Long userId, @Param("avatar") String avatar);

    @Update("UPDATE user SET grade = #{grade}, gpa = #{gpa}, update_time = NOW() WHERE id = #{userId}")
    int updateStudentAcademic(@Param("userId") Long userId,
                              @Param("grade") Integer grade,
                              @Param("gpa") java.math.BigDecimal gpa);

    @Select("SELECT * FROM user WHERE id = #{id}")
    UserEntity selectUserById(Long id);
}
